package org.kurron.hard.parts.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.order.core.OrderDomainService
import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.SagaEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OrderChoreographyTests {

    private val service = OrderDomainService()

    @Test
    fun `duplicate events are ignored`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU", BigDecimal.TEN, "PLACED", "corr-1"))
        val event = SagaEvent("evt-1", "corr-1", orderId, "payment.processed", Instant.now(), emptyMap())

        service.applyEvent(event)
        service.applyEvent(event)

        assertEquals("PAYMENT_OK", service.find(orderId)?.status)
    }

    @Test
    fun `out-of-order events reconcile eventually`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU", BigDecimal.TEN, "PLACED", "corr-1"))

        service.applyEvent(SagaEvent("evt-2", "corr-1", orderId, "fulfillment.completed", Instant.now(), emptyMap()))
        assertEquals("PLACED", service.find(orderId)?.status)

        service.applyEvent(SagaEvent("evt-3", "corr-1", orderId, "payment.processed", Instant.now(), emptyMap()))
        service.reconcile()

        assertEquals("FULFILLMENT_OK", service.find(orderId)?.status)
    }

    @Test
    fun `delayed email event completes order`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU", BigDecimal.TEN, "PAYMENT_OK", "corr-1"))

        service.applyEvent(SagaEvent("evt-4", "corr-1", orderId, "email.sent", Instant.now().plusSeconds(60), emptyMap()))

        assertEquals("EMAIL_OK", service.find(orderId)?.status)
    }
}
