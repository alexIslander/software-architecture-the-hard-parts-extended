package org.kurron.hard.parts.fulfillment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.fulfillment.core.FulfillmentDomainService
import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.FulfillmentCommand
import java.time.Instant
import java.util.UUID

class FulfillmentDomainServiceTests {

    private val service = FulfillmentDomainService()

    @Test
    fun `reserve is idempotent for repeated idempotency key`() {
        val orderId = UUID.randomUUID()
        val first = service.reserve(FulfillmentCommand(orderId, "SKU-1", "corr-1", "idem-1"))
        val second = service.reserve(FulfillmentCommand(orderId, "SKU-1", "corr-1", "idem-1", failRequest = true))

        assertTrue(first.success)
        assertEquals(first, second)
        assertEquals("RESERVED", service.status(orderId))
    }

    @Test
    fun `email-stage failure triggers compensation once`() {
        val orderId = UUID.randomUUID()
        service.reserve(FulfillmentCommand(orderId, "SKU-2", "corr-2", "idem-2"))
        service.complete(orderId)

        val event = FailureEvent(
            eventId = "fail-1",
            correlationId = "corr-2",
            orderId = orderId,
            stage = FailureStage.EMAIL,
            reason = "smtp timeout",
            createdAt = Instant.now()
        )

        val firstCompensation = service.compensationFromFailure(event)
        val duplicateCompensation = service.compensationFromFailure(event)

        assertNotNull(firstCompensation)
        assertNull(duplicateCompensation)
        assertEquals("CANCELLED", service.status(orderId))
    }

    @Test
    fun `non-email failure does not trigger compensation`() {
        val orderId = UUID.randomUUID()
        val event = FailureEvent(
            eventId = "fail-2",
            correlationId = "corr-3",
            orderId = orderId,
            stage = FailureStage.PAYMENT,
            reason = "declined",
            createdAt = Instant.now()
        )

        val compensation = service.compensationFromFailure(event)
        assertNull(compensation)
        assertFalse(service.markProcessedEvent("fail-2"), "event must be deduplicated after first processing")
    }
}
