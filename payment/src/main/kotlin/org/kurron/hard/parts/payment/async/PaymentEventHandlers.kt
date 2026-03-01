package org.kurron.hard.parts.payment.async

import org.kurron.hard.parts.payment.core.PaymentDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.PaymentCommand
import org.kurron.hard.parts.shared.SagaEvent
import org.kurron.hard.parts.shared.SagaTopics
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Component
class PaymentEventHandlers(
    private val paymentDomainService: PaymentDomainService,
    private val rabbitTemplate: RabbitTemplate
) {

    @RabbitListener(queues = ["payment.events"])
    fun onOrderPlaced(event: SagaEvent) {
        if (!paymentDomainService.markProcessedEvent(event.eventId)) {
            return
        }

        val amount = BigDecimal(event.payload["amount"].toString())
        val outcome = paymentDomainService.charge(
            PaymentCommand(
                orderId = event.orderId,
                amount = amount,
                correlationId = event.correlationId,
                idempotencyKey = "event-${event.eventId}"
            )
        )

        if (outcome.success) {
            rabbitTemplate.convertAndSend(
                SagaTopics.EXCHANGE,
                SagaTopics.PAYMENT_PROCESSED,
                SagaEvent(
                    eventId = UUID.randomUUID().toString(),
                    correlationId = event.correlationId,
                    orderId = event.orderId,
                    eventType = SagaTopics.PAYMENT_PROCESSED,
                    createdAt = Instant.now(),
                    payload = mapOf("status" to "PAID")
                )
            )
        } else {
            rabbitTemplate.convertAndSend(
                SagaTopics.EXCHANGE,
                SagaTopics.FAILURE,
                FailureEvent(
                    eventId = UUID.randomUUID().toString(),
                    correlationId = event.correlationId,
                    orderId = event.orderId,
                    stage = FailureStage.PAYMENT,
                    reason = outcome.details,
                    createdAt = Instant.now()
                )
            )
        }
    }

    @RabbitListener(queues = ["payment.failures"])
    fun onFailure(event: FailureEvent) {
        paymentDomainService.compensationFromFailure(event)
    }
}
