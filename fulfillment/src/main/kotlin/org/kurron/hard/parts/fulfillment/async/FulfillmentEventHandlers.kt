package org.kurron.hard.parts.fulfillment.async

import org.kurron.hard.parts.fulfillment.core.FulfillmentDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.FulfillmentCommand
import org.kurron.hard.parts.shared.SagaEvent
import org.kurron.hard.parts.shared.SagaTopics
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class FulfillmentEventHandlers(
    private val fulfillment: FulfillmentDomainService,
    private val rabbitTemplate: RabbitTemplate
) {

    @RabbitListener(queues = ["fulfillment.events"])
    fun onPaymentProcessed(event: SagaEvent) {
        if (!fulfillment.markProcessedEvent(event.eventId)) {
            return
        }

        val itemSku = event.payload["itemSku"]?.toString() ?: "UNKNOWN"
        val outcome = fulfillment.reserve(
            FulfillmentCommand(
                orderId = event.orderId,
                itemSku = itemSku,
                correlationId = event.correlationId,
                idempotencyKey = "event-${event.eventId}"
            )
        )

        if (outcome.success) {
            fulfillment.complete(event.orderId)
            rabbitTemplate.convertAndSend(
                SagaTopics.EXCHANGE,
                SagaTopics.FULFILLMENT_COMPLETED,
                SagaEvent(
                    eventId = UUID.randomUUID().toString(),
                    correlationId = event.correlationId,
                    orderId = event.orderId,
                    eventType = SagaTopics.FULFILLMENT_COMPLETED,
                    createdAt = Instant.now(),
                    payload = mapOf("itemSku" to itemSku)
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
                    stage = FailureStage.FULFILLMENT,
                    reason = outcome.details,
                    createdAt = Instant.now()
                )
            )
        }
    }

    @RabbitListener(queues = ["fulfillment.failures"])
    fun onFailure(event: FailureEvent) {
        fulfillment.compensationFromFailure(event)
    }
}
