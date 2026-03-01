package org.kurron.hard.parts.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.order.core.OrderDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.SagaEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class AnthologySagaProjectionTests {

    private val service = OrderDomainService()

    @Test
    fun `projection keeps pending count until dependencies arrive`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU-A", BigDecimal.TEN, "PLACED", "corr-a"))

        service.applyEvent(
            SagaEvent("evt-a1", "corr-a", orderId, "fulfillment.completed", Instant.now(), emptyMap())
        )

        assertEquals("PLACED", service.find(orderId)?.status)
        assertEquals(1, service.pendingEventCount(orderId))

        service.applyEvent(
            SagaEvent("evt-a2", "corr-a", orderId, "payment.processed", Instant.now(), emptyMap())
        )
        service.reconcile()

        assertEquals("FULFILLMENT_OK", service.find(orderId)?.status)
        assertEquals(0, service.pendingEventCount(orderId))
    }

    @Test
    fun `failure event marks projection and duplicate is ignored`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU-B", BigDecimal.ONE, "PLACED", "corr-b"))
        val failure = FailureEvent(
            "fail-b1",
            "corr-b",
            orderId,
            FailureStage.EMAIL,
            "smtp timeout",
            Instant.now()
        )

        service.applyFailure(failure)
        service.applyFailure(failure)

        assertEquals("FAILED:EMAIL", service.find(orderId)?.status)
    }

    @Test
    fun `late email event finalizes after success stages`() {
        val orderId = UUID.randomUUID()
        service.create(OrderRecord(orderId, "a@b.com", "SKU-C", BigDecimal.TEN, "PLACED", "corr-c"))
        service.applyEvent(SagaEvent("evt-c1", "corr-c", orderId, "payment.processed", Instant.now(), emptyMap()))
        service.applyEvent(SagaEvent("evt-c2", "corr-c", orderId, "fulfillment.completed", Instant.now(), emptyMap()))
        service.applyEvent(
            SagaEvent("evt-c3", "corr-c", orderId, "email.sent", Instant.now().plusSeconds(30), emptyMap())
        )

        assertEquals("EMAIL_OK", service.find(orderId)?.status)
    }
}
