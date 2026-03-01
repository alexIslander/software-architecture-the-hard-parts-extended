package org.kurron.hard.parts.email

import org.junit.jupiter.api.Test
import org.kurron.hard.parts.email.async.EmailEventHandlers
import org.kurron.hard.parts.email.core.EmailDomainService
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

class EmailEventHandlersTests {

    private val rabbitTemplate = mock(RabbitTemplate::class.java)
    private val domainService = EmailDomainService()
    private val handlers = EmailEventHandlers(domainService, rabbitTemplate)

    @Test
    fun `fulfillment completed publishes email sent event`() {
        val orderId = UUID.randomUUID()
        handlers.onFulfillmentCompleted(
            SagaEvent(
                eventId = "evt-email-1",
                correlationId = "corr-1",
                orderId = orderId,
                eventType = SagaTopics.FULFILLMENT_COMPLETED,
                createdAt = Instant.now(),
                payload = mapOf("customerEmail" to "buyer@example.com")
            )
        )

        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)
        verify(rabbitTemplate, times(1))
            .convertAndSend(eq(SagaTopics.EXCHANGE), eq(SagaTopics.EMAIL_SENT), payloadCaptor.capture())
    }

    @Test
    fun `duplicate event is ignored`() {
        val orderId = UUID.randomUUID()
        val event = SagaEvent(
            eventId = "evt-email-2",
            correlationId = "corr-2",
            orderId = orderId,
            eventType = SagaTopics.FULFILLMENT_COMPLETED,
            createdAt = Instant.now(),
            payload = mapOf("customerEmail" to "buyer@example.com")
        )

        handlers.onFulfillmentCompleted(event)
        handlers.onFulfillmentCompleted(event)

        val payloadCaptor = ArgumentCaptor.forClass(Any::class.java)
        verify(rabbitTemplate, times(1))
            .convertAndSend(eq(SagaTopics.EXCHANGE), eq(SagaTopics.EMAIL_SENT), payloadCaptor.capture())
    }
}
