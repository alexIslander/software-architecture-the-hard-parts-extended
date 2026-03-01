package org.kurron.hard.parts.order.core

import org.kurron.hard.parts.order.config.PhoneTagSagaProperties
import org.kurron.hard.parts.shared.EmailCommand
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.FulfillmentCommand
import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.PaymentCommand
import org.kurron.hard.parts.shared.PurchaseRequest
import org.kurron.hard.parts.shared.ServiceResult
import org.kurron.hard.parts.shared.WorkflowStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Instant
import java.util.UUID

@Service
class PhoneTagSagaService(
    private val orders: OrderDomainService,
    private val store: PhoneTagWorkflowStore,
    private val restClient: RestClient,
    private val properties: PhoneTagSagaProperties
) {

    fun purchase(request: PurchaseRequest): PhoneTagWorkflowState {
        val existing = store.findByIdempotencyKey(request.idempotencyKey)
        if (existing != null && existing.status != WorkflowStatus.FAILED) {
            return existing
        }

        val state = existing ?: save(
            PhoneTagWorkflowState(
                workflowId = UUID.randomUUID(),
                orderId = request.orderId,
                correlationId = UUID.randomUUID().toString(),
                idempotencyKey = request.idempotencyKey,
                status = WorkflowStatus.STARTED,
                lastError = null,
                updatedAt = Instant.now()
            )
        )
        return continueFromState(state, request)
    }

    fun resume(workflowId: UUID, request: PurchaseRequest?): PhoneTagWorkflowState {
        val existing = store.findByWorkflowId(workflowId) ?: error("unknown workflowId: $workflowId")
        val state = if (existing.status == WorkflowStatus.FAILED && existing.lastError?.contains("|") == true) {
            val previous = existing.lastError.substringBefore("|")
            save(existing.copy(status = WorkflowStatus.valueOf(previous), lastError = existing.lastError.substringAfter("|")))
        } else {
            existing
        }
        return continueFromState(state, request)
    }

    private fun continueFromState(
        existing: PhoneTagWorkflowState,
        request: PurchaseRequest?
    ): PhoneTagWorkflowState {
        var state = existing
        val purchase = request ?: PurchaseRequest(
            orderId = state.orderId,
            customerEmail = "recovered@example.com",
            itemSku = "UNKNOWN",
            amount = java.math.BigDecimal.ZERO,
            idempotencyKey = state.idempotencyKey
        )

        return try {
            if (state.status == WorkflowStatus.STARTED) {
                orders.create(
                    OrderRecord(
                        orderId = purchase.orderId,
                        customerEmail = purchase.customerEmail,
                        itemSku = purchase.itemSku,
                        amount = purchase.amount,
                        status = "PLACED",
                        correlationId = state.correlationId
                    )
                )
                state = save(state.copy(status = WorkflowStatus.ORDER_OK, lastError = null))
            }

            if (state.status == WorkflowStatus.ORDER_OK) {
                val paymentResult = postWithRetry(
                    "${properties.paymentUrl}/internal/payments/charge",
                    PaymentCommand(
                        orderId = purchase.orderId,
                        amount = purchase.amount,
                        correlationId = state.correlationId,
                        idempotencyKey = state.idempotencyKey,
                        failRequest = purchase.failAtStage == FailureStage.PAYMENT
                    )
                )
                if (!paymentResult.success) {
                    return fail(state, "payment failure: ${paymentResult.details}")
                }
                orders.markStatus(purchase.orderId, "PAYMENT_OK")
                state = save(state.copy(status = WorkflowStatus.PAYMENT_OK, lastError = null))
            }

            if (state.status == WorkflowStatus.PAYMENT_OK) {
                val fulfillmentResult = postWithRetry(
                    "${properties.fulfillmentUrl}/internal/fulfillment/reserve",
                    FulfillmentCommand(
                        orderId = purchase.orderId,
                        itemSku = purchase.itemSku,
                        correlationId = state.correlationId,
                        idempotencyKey = state.idempotencyKey,
                        failRequest = purchase.failAtStage == FailureStage.FULFILLMENT
                    )
                )
                if (!fulfillmentResult.success) {
                    compensatePayment(state, purchase)
                    orders.markStatus(purchase.orderId, "COMPENSATED")
                    return save(
                        state.copy(
                            status = WorkflowStatus.COMPENSATED,
                            lastError = "fulfillment failure: ${fulfillmentResult.details}"
                        )
                    )
                }
                orders.markStatus(purchase.orderId, "FULFILLMENT_OK")
                state = save(state.copy(status = WorkflowStatus.FULFILLMENT_OK, lastError = null))
            }

            if (state.status == WorkflowStatus.FULFILLMENT_OK) {
                val emailResult = postWithRetry(
                    "${properties.emailUrl}/internal/email/send",
                    EmailCommand(
                        orderId = purchase.orderId,
                        customerEmail = purchase.customerEmail,
                        correlationId = state.correlationId,
                        idempotencyKey = state.idempotencyKey,
                        failRequest = purchase.failAtStage == FailureStage.EMAIL
                    )
                )
                if (!emailResult.success) {
                    compensateFulfillment(state, purchase)
                    compensatePayment(state, purchase)
                    orders.markStatus(purchase.orderId, "COMPENSATED")
                    return save(
                        state.copy(
                            status = WorkflowStatus.COMPENSATED,
                            lastError = "email failure: ${emailResult.details}"
                        )
                    )
                }
                orders.markStatus(purchase.orderId, "EMAIL_OK")
                return save(state.copy(status = WorkflowStatus.EMAIL_OK, lastError = null))
            }

            state
        } catch (ex: Exception) {
            fail(state, ex.message ?: "unknown phone-tag failure")
        }
    }

    private fun compensatePayment(state: PhoneTagWorkflowState, request: PurchaseRequest) {
        postWithRetry(
            "${properties.paymentUrl}/internal/payments/refund",
            PaymentCommand(
                orderId = request.orderId,
                amount = request.amount,
                correlationId = state.correlationId,
                idempotencyKey = "${state.idempotencyKey}-refund"
            )
        )
    }

    private fun compensateFulfillment(state: PhoneTagWorkflowState, request: PurchaseRequest) {
        postWithRetry(
            "${properties.fulfillmentUrl}/internal/fulfillment/cancel",
            FulfillmentCommand(
                orderId = request.orderId,
                itemSku = request.itemSku,
                correlationId = state.correlationId,
                idempotencyKey = "${state.idempotencyKey}-cancel"
            )
        )
    }

    private fun fail(state: PhoneTagWorkflowState, message: String): PhoneTagWorkflowState {
        return save(state.copy(status = WorkflowStatus.FAILED, lastError = "${state.status}|$message"))
    }

    private fun save(state: PhoneTagWorkflowState): PhoneTagWorkflowState {
        return store.save(state.copy(updatedAt = Instant.now()))
    }

    private inline fun <reified TRequest : Any> postWithRetry(url: String, body: TRequest): ServiceResult {
        var lastException: Exception? = null
        repeat(properties.retryMaxAttempts) {
            try {
                val response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ServiceResult::class.java)
                if (response != null) {
                    return response
                }
                return ServiceResult(extractOrderId(body), "", false, "empty response")
            } catch (ex: RestClientException) {
                lastException = ex
            }
        }
        throw lastException ?: IllegalStateException("retry failed for $url")
    }

    private fun extractOrderId(body: Any): UUID {
        return when (body) {
            is PaymentCommand -> body.orderId
            is FulfillmentCommand -> body.orderId
            is EmailCommand -> body.orderId
            is OrderRecord -> body.orderId
            else -> UUID.randomUUID()
        }
    }
}
