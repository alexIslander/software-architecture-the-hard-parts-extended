package org.kurron.hard.parts.email.async

import org.kurron.hard.parts.email.core.EmailDomainService
import org.kurron.hard.parts.shared.EmailCommand
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.SagaEvent
import org.kurron.hard.parts.shared.SagaTopics
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class EmailEventHandlers(
    private val emails: EmailDomainService,
    private val rabbitTemplate: RabbitTemplate
) {

    @RabbitListener(queues = ["email.events"])
    fun onFulfillmentCompleted(event: SagaEvent) {
        if (!emails.markProcessedEvent(event.eventId)) {
            return
        }

        val customerEmail = event.payload["customerEmail"]?.toString() ?: "unknown@example.com"
        val outcome = emails.send(
            EmailCommand(
                orderId = event.orderId,
                customerEmail = customerEmail,
                correlationId = event.correlationId,
                idempotencyKey = "event-${event.eventId}"
            )
        )

        if (outcome.success) {
            rabbitTemplate.convertAndSend(
                SagaTopics.EXCHANGE,
                SagaTopics.EMAIL_SENT,
                SagaEvent(
                    eventId = UUID.randomUUID().toString(),
                    correlationId = event.correlationId,
                    orderId = event.orderId,
                    eventType = SagaTopics.EMAIL_SENT,
                    createdAt = Instant.now(),
                    payload = mapOf("status" to "SENT")
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
                    stage = FailureStage.EMAIL,
                    reason = outcome.details,
                    createdAt = Instant.now()
                )
            )
        }
    }
}
