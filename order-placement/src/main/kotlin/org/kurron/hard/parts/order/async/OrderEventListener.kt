package org.kurron.hard.parts.order.async

import org.kurron.hard.parts.order.core.OrderDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.SagaEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class OrderEventListener(
    private val service: OrderDomainService
) {

    @RabbitListener(queues = ["order.events"])
    fun onSagaEvent(event: SagaEvent) {
        service.applyEvent(event)
    }

    @RabbitListener(queues = ["order.failures"])
    fun onFailure(event: FailureEvent) {
        service.applyFailure(event)
    }
}
