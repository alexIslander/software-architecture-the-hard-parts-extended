package org.kurron.hard.parts.shared

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

object SagaTopics {
    const val EXCHANGE = "saga.events"
    const val ORDER_PLACED = "order.placed"
    const val PAYMENT_PROCESSED = "payment.processed"
    const val FULFILLMENT_COMPLETED = "fulfillment.completed"
    const val EMAIL_SENT = "email.sent"
    const val FAILURE = "saga.failed"
}

enum class WorkflowStatus {
    STARTED,
    ORDER_OK,
    PAYMENT_OK,
    FULFILLMENT_OK,
    EMAIL_OK,
    FAILED,
    COMPENSATED
}

enum class FailureStage {
    ORDER,
    PAYMENT,
    FULFILLMENT,
    EMAIL
}

data class PurchaseRequest(
    val orderId: UUID = UUID.randomUUID(),
    val customerEmail: String,
    val itemSku: String,
    val amount: BigDecimal,
    val idempotencyKey: String,
    val failAtStage: FailureStage? = null
)

data class OrderRecord(
    val orderId: UUID,
    val customerEmail: String,
    val itemSku: String,
    val amount: BigDecimal,
    val status: String,
    val correlationId: String
)

data class PaymentCommand(
    val orderId: UUID,
    val amount: BigDecimal,
    val correlationId: String,
    val idempotencyKey: String,
    val failRequest: Boolean = false
)

data class FulfillmentCommand(
    val orderId: UUID,
    val itemSku: String,
    val correlationId: String,
    val idempotencyKey: String,
    val failRequest: Boolean = false
)

data class EmailCommand(
    val orderId: UUID,
    val customerEmail: String,
    val correlationId: String,
    val idempotencyKey: String,
    val failRequest: Boolean = false
)

data class ServiceResult(
    val orderId: UUID,
    val correlationId: String,
    val success: Boolean,
    val details: String
)

data class SagaEvent(
    val eventId: String,
    val correlationId: String,
    val orderId: UUID,
    val eventType: String,
    val createdAt: Instant,
    val payload: Map<String, Any?>
)

data class FailureEvent(
    val eventId: String,
    val correlationId: String,
    val orderId: UUID,
    val stage: FailureStage,
    val reason: String,
    val createdAt: Instant
)
