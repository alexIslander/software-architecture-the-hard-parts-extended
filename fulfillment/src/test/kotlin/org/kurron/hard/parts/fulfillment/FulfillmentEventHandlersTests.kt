package org.kurron.hard.parts.fulfillment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.fulfillment.async.FulfillmentEventHandlers
import org.kurron.hard.parts.fulfillment.core.FulfillmentDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.SagaEvent
import org.kurron.hard.parts.shared.SagaTopics
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.Instant
import java.util.UUID

class FulfillmentEventHandlersTests {

    private val rabbitTemplate = mock(RabbitTemplate::class.java)
    private val domainService = FulfillmentDomainService()
    private val handlers = FulfillmentEventHandlers(domainService, rabbitTemplate)

    @Test
    fun `payment processed publishes fulfillment completed event`() {
        val orderId = UUID.randomUUID()
        val event = SagaEvent(
            eventId = "evt-fulfill-1",
            correlationId = "corr-1",
            orderId = orderId,
            eventType = SagaTopics.PAYMENT_PROCESSED,
            createdAt = Instant.now(),
            payload = mapOf("itemSku" to "SKU-1")
        )

        handlers.onPaymentProcessed(event)

        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)
        verify(rabbitTemplate, times(1))
            .convertAndSend(eq(SagaTopics.EXCHANGE), eq(SagaTopics.FULFILLMENT_COMPLETED), payloadCaptor.capture())
        assertEquals("COMPLETED", domainService.status(orderId))
    }

    @Test
    fun `duplicate event is ignored`() {
        val orderId = UUID.randomUUID()
        val event = SagaEvent(
            eventId = "evt-fulfill-2",
            correlationId = "corr-2",
            orderId = orderId,
            eventType = SagaTopics.PAYMENT_PROCESSED,
            createdAt = Instant.now(),
            payload = mapOf("itemSku" to "SKU-2")
        )

        handlers.onPaymentProcessed(event)
        handlers.onPaymentProcessed(event)

        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)
        verify(rabbitTemplate, times(1))
            .convertAndSend(eq(SagaTopics.EXCHANGE), eq(SagaTopics.FULFILLMENT_COMPLETED), payloadCaptor.capture())
    }

    @Test
    fun `failure event triggers compensation for email stage`() {
        val orderId = UUID.randomUUID()
        domainService.reserve(org.kurron.hard.parts.shared.FulfillmentCommand(orderId, "SKU-3", "corr-3", "idem-3"))
        domainService.complete(orderId)

        handlers.onFailure(
            FailureEvent(
                eventId = "evt-failure-1",
                correlationId = "corr-3",
                orderId = orderId,
                stage = FailureStage.EMAIL,
                reason = "smtp timeout",
                createdAt = Instant.now()
            )
        )

        assertEquals("CANCELLED", domainService.status(orderId))
    }
}
