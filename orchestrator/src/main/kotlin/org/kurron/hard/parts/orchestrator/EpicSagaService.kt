package org.kurron.hard.parts.orchestrator

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
class EpicSagaService(
    private val store: WorkflowStore,
    private val restClient: RestClient,
    private val properties: OrchestratorProperties
) {

    fun purchase(request: PurchaseRequest): WorkflowState {
        val existing = store.findByIdempotencyKey(request.idempotencyKey)
        if (existing != null && existing.status != WorkflowStatus.FAILED) {
            return existing
        }

        val state = existing ?: store.save(
            WorkflowState(
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

    fun resume(workflowId: UUID, request: PurchaseRequest?): WorkflowState {
        val state = store.findByWorkflowId(workflowId) ?: error("unknown workflowId: $workflowId")
        val resumableState = if (state.status == WorkflowStatus.FAILED && state.lastError?.contains("|") == true) {
            val previous = state.lastError.substringBefore("|")
            update(state, WorkflowStatus.valueOf(previous), state.lastError.substringAfter("|"))
        } else {
            state
        }
        return continueFromState(resumableState, request)
    }

    private fun continueFromState(existing: WorkflowState, request: PurchaseRequest?): WorkflowState {
        var state = existing
        val purchase = request ?: PurchaseRequest(
            orderId = state.orderId,
            customerEmail = "recovered@example.com",
            itemSku = "UNKNOWN",
            amount = java.math.BigDecimal.ZERO,
            idempotencyKey = state.idempotencyKey,
            failAtStage = null
        )

        try {
            if (state.status == WorkflowStatus.STARTED) {
                postWithRetry(
                    "${properties.orderUrl}/internal/orders",
                    OrderRecord(
                        orderId = purchase.orderId,
                        customerEmail = purchase.customerEmail,
                        itemSku = purchase.itemSku,
                        amount = purchase.amount,
                        status = "PLACED",
                        correlationId = state.correlationId
                    )
                )
                state = update(state, WorkflowStatus.ORDER_OK)
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
                state = update(state, WorkflowStatus.PAYMENT_OK)
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
                    return update(
                        update(state, WorkflowStatus.COMPENSATED),
                        WorkflowStatus.COMPENSATED,
                        "fulfillment failure: ${fulfillmentResult.details}"
                    )
                }
                state = update(state, WorkflowStatus.FULFILLMENT_OK)
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
                    return update(
                        update(state, WorkflowStatus.COMPENSATED),
                        WorkflowStatus.COMPENSATED,
                        "email failure: ${emailResult.details}"
                    )
                }
                state = update(state, WorkflowStatus.EMAIL_OK)
            }

            return state
        } catch (ex: Exception) {
            return fail(state, ex.message ?: "unknown orchestration failure")
        }
    }

    private fun update(state: WorkflowState, newStatus: WorkflowStatus, error: String? = null): WorkflowState {
        return store.save(
            state.copy(
                status = newStatus,
                lastError = error,
                updatedAt = Instant.now()
            )
        )
    }

    private fun fail(state: WorkflowState, message: String): WorkflowState {
        return update(state, WorkflowStatus.FAILED, "${state.status}|$message")
    }

    private fun compensatePayment(state: WorkflowState, request: PurchaseRequest) {
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

    private fun compensateFulfillment(state: WorkflowState, request: PurchaseRequest) {
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
