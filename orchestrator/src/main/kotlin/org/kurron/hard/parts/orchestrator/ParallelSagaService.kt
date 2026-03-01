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
class ParallelSagaService(
    private val store: ParallelWorkflowStore,
    private val restClient: RestClient,
    private val properties: OrchestratorProperties
) {

    fun purchase(request: PurchaseRequest): ParallelWorkflowState {
        val existing = store.findByIdempotencyKey(request.idempotencyKey)
        if (existing != null && existing.status != WorkflowStatus.FAILED) {
            return existing
        }
        return existing ?: store.save(
            ParallelWorkflowState(
                workflowId = UUID.randomUUID(),
                orderId = request.orderId,
                correlationId = UUID.randomUUID().toString(),
                idempotencyKey = request.idempotencyKey,
                status = WorkflowStatus.STARTED,
                orderPlaced = false,
                paymentDone = false,
                fulfillmentDone = false,
                emailDone = false,
                lastError = null,
                updatedAt = Instant.now()
            )
        )
    }

    fun advance(workflowId: UUID, request: PurchaseRequest?): ParallelWorkflowState {
        val existing = store.findByWorkflowId(workflowId) ?: error("unknown workflowId: $workflowId")
        val state = resumeIfFailed(existing)
        val purchase = request ?: PurchaseRequest(
            orderId = state.orderId,
            customerEmail = "recovered@example.com",
            itemSku = "UNKNOWN",
            amount = java.math.BigDecimal.ZERO,
            idempotencyKey = state.idempotencyKey,
            failAtStage = null
        )

        if (!state.orderPlaced) {
            val orderResult = runCatching {
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
            }.getOrElse { return fail(state, WorkflowStatus.STARTED, it.message ?: "order call failed") }

            if (!orderResult.success) {
                return fail(state, WorkflowStatus.STARTED, "order failure: ${orderResult.details}")
            }
            return save(state.copy(orderPlaced = true, status = WorkflowStatus.ORDER_OK, lastError = null))
        }

        if (!state.paymentDone || !state.fulfillmentDone) {
            var next = state
            var paymentError: String? = null
            var fulfillmentError: String? = null

            if (!state.paymentDone) {
                val paymentResult = runCatching {
                    postWithRetry(
                        "${properties.paymentUrl}/internal/payments/charge",
                        PaymentCommand(
                            orderId = purchase.orderId,
                            amount = purchase.amount,
                            correlationId = state.correlationId,
                            idempotencyKey = "${state.idempotencyKey}-parallel-payment",
                            failRequest = purchase.failAtStage == FailureStage.PAYMENT
                        )
                    )
                }.getOrElse {
                    paymentError = it.message ?: "payment call failed"
                    null
                }
                if (paymentResult != null) {
                    if (paymentResult.success) {
                        next = next.copy(paymentDone = true)
                    } else {
                        paymentError = paymentResult.details
                    }
                }
            }

            if (!state.fulfillmentDone) {
                val fulfillmentResult = runCatching {
                    postWithRetry(
                        "${properties.fulfillmentUrl}/internal/fulfillment/reserve",
                        FulfillmentCommand(
                            orderId = purchase.orderId,
                            itemSku = purchase.itemSku,
                            correlationId = state.correlationId,
                            idempotencyKey = "${state.idempotencyKey}-parallel-fulfillment",
                            failRequest = purchase.failAtStage == FailureStage.FULFILLMENT
                        )
                    )
                }.getOrElse {
                    fulfillmentError = it.message ?: "fulfillment call failed"
                    null
                }
                if (fulfillmentResult != null) {
                    if (fulfillmentResult.success) {
                        next = next.copy(fulfillmentDone = true)
                    } else {
                        fulfillmentError = fulfillmentResult.details
                    }
                }
            }

            val progressed = if (next.paymentDone && next.fulfillmentDone) {
                save(next.copy(status = WorkflowStatus.FULFILLMENT_OK, lastError = null))
            } else if (next.paymentDone) {
                save(next.copy(status = WorkflowStatus.PAYMENT_OK, lastError = null))
            } else {
                save(next.copy(status = WorkflowStatus.ORDER_OK, lastError = null))
            }

            val errors = listOfNotNull(paymentError?.let { "payment=$it" }, fulfillmentError?.let { "fulfillment=$it" })
            if (errors.isNotEmpty()) {
                return fail(progressed, progressed.status, "parallel stage failure: ${errors.joinToString(", ")}")
            }
            return progressed
        }

        if (!state.emailDone) {
            val emailResult = runCatching {
                postWithRetry(
                    "${properties.emailUrl}/internal/email/send",
                    EmailCommand(
                        orderId = purchase.orderId,
                        customerEmail = purchase.customerEmail,
                        correlationId = state.correlationId,
                        idempotencyKey = "${state.idempotencyKey}-parallel-email",
                        failRequest = purchase.failAtStage == FailureStage.EMAIL
                    )
                )
            }.getOrElse { return fail(state, WorkflowStatus.FULFILLMENT_OK, it.message ?: "email call failed") }

            if (!emailResult.success) {
                return fail(state, WorkflowStatus.FULFILLMENT_OK, "email failure: ${emailResult.details}")
            }
            return save(state.copy(emailDone = true, status = WorkflowStatus.EMAIL_OK, lastError = null))
        }

        return state
    }

    fun resume(workflowId: UUID, request: PurchaseRequest?): ParallelWorkflowState = advance(workflowId, request)

    private fun resumeIfFailed(state: ParallelWorkflowState): ParallelWorkflowState {
        if (state.status != WorkflowStatus.FAILED || state.lastError?.contains("|") != true) {
            return state
        }
        val previous = WorkflowStatus.valueOf(state.lastError.substringBefore("|"))
        val message = state.lastError.substringAfter("|")
        return save(state.copy(status = previous, lastError = message))
    }

    private fun fail(state: ParallelWorkflowState, previous: WorkflowStatus, message: String): ParallelWorkflowState {
        return save(state.copy(status = WorkflowStatus.FAILED, lastError = "$previous|$message"))
    }

    private fun save(state: ParallelWorkflowState): ParallelWorkflowState {
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
