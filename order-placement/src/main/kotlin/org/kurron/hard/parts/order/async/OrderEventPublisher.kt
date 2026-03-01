package org.kurron.hard.parts.order.async

import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.SagaEvent
import org.kurron.hard.parts.shared.SagaTopics
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class OrderEventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {

    fun publishOrderPlaced(order: OrderRecord) {
        rabbitTemplate.convertAndSend(
            SagaTopics.EXCHANGE,
            SagaTopics.ORDER_PLACED,
            SagaEvent(
                eventId = UUID.randomUUID().toString(),
                correlationId = order.correlationId,
                orderId = order.orderId,
                eventType = SagaTopics.ORDER_PLACED,
                createdAt = Instant.now(),
                payload = mapOf(
                    "customerEmail" to order.customerEmail,
                    "itemSku" to order.itemSku,
                    "amount" to order.amount.toPlainString()
                )
            )
        )
    }
}
