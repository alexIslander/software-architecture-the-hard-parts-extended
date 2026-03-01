package org.kurron.hard.parts.email

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.email.core.EmailDomainService
import org.kurron.hard.parts.shared.EmailCommand
import java.util.UUID

class EmailDomainServiceTests {

    private val service = EmailDomainService()

    @Test
    fun `send is idempotent for repeated key`() {
        val orderId = UUID.randomUUID()
        val first = service.send(EmailCommand(orderId, "a@example.com", "corr-1", "idem-1"))
        val second = service.send(EmailCommand(orderId, "b@example.com", "corr-1", "idem-1", failRequest = true))

        assertTrue(first.success)
        assertEquals(first, second)
        assertEquals("SENT:a@example.com", service.status(orderId))
    }

    @Test
    fun `failed send leaves status unchanged`() {
        val orderId = UUID.randomUUID()

        val result = service.send(EmailCommand(orderId, "a@example.com", "corr-2", "idem-2", failRequest = true))

        assertFalse(result.success)
        assertEquals("NONE", service.status(orderId))
    }

    @Test
    fun `processed events are deduplicated`() {
        assertTrue(service.markProcessedEvent("evt-1"))
        assertFalse(service.markProcessedEvent("evt-1"))
    }
}
