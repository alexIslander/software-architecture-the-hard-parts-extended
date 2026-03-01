package org.kurron.hard.parts.order.api

import org.kurron.hard.parts.order.core.OrderDomainService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/anthology")
class AnthologySagaController(
    private val orders: OrderDomainService
) {

    @GetMapping("/orders/{orderId}/projection")
    fun projection(@PathVariable orderId: UUID): Map<String, Any?> {
        val order = orders.find(orderId)
        return mapOf(
            "orderId" to orderId,
            "exists" to (order != null),
            "status" to order?.status,
            "pendingEvents" to orders.pendingEventCount(orderId)
        )
    }

    @PostMapping("/reconcile")
    fun reconcile(): Map<String, String> {
        orders.reconcile()
        return mapOf("result" to "reconciled")
    }
}
