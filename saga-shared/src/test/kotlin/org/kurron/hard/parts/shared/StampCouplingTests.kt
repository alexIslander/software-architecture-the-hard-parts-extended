package org.kurron.hard.parts.shared

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StampCouplingTests {

    @Test
    fun `explicit payload avoids stamp coupling`() {
        val payload = mapOf("orderId" to "o-1", "status" to "PAID")
        assertTrue(payload.keys.containsAll(listOf("orderId", "status")))
        assertFalse(payload.containsKey("internalRiskScore"))
    }

    @Test
    fun `large aggregate payload demonstrates anti-example`() {
        val antiExample = mapOf(
            "orderId" to "o-2",
            "customer" to mapOf("name" to "x", "tier" to "gold"),
            "auditTrail" to listOf("a", "b"),
            "internalRiskScore" to 93,
            "debugFlags" to mapOf("manualReview" to false)
        )

        assertTrue(antiExample.containsKey("internalRiskScore"))
    }
}
