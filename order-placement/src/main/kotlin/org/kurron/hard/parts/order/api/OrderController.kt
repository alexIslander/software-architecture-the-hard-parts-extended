package org.kurron.hard.parts.order.api

import org.kurron.hard.parts.order.async.OrderEventPublisher
import org.kurron.hard.parts.order.core.OrderDomainService
import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.PurchaseRequest
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping
class OrderController(
    private val orders: OrderDomainService,
    private val publisher: OrderEventPublisher
) {

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun placeOrder(@RequestBody request: PurchaseRequest): ServiceResult {
        val created = orders.create(
            OrderRecord(
                orderId = request.orderId,
                customerEmail = request.customerEmail,
                itemSku = request.itemSku,
                amount = request.amount,
                status = "PLACED",
                correlationId = request.idempotencyKey
            )
        )
        publisher.publishOrderPlaced(created)
        return ServiceResult(created.orderId, created.correlationId, true, "order accepted")
    }

    @PostMapping("/internal/orders")
    fun createInternalOrder(@RequestBody record: OrderRecord): ServiceResult {
        val created = orders.create(record)
        return ServiceResult(created.orderId, created.correlationId, true, "order created")
    }

    @PostMapping("/internal/orders/{orderId}/status/{status}")
    fun updateStatus(@PathVariable orderId: UUID, @PathVariable status: String): ServiceResult {
        val updated = orders.markStatus(orderId, status)
        return ServiceResult(orderId, "", updated != null, updated?.status ?: "unknown order")
    }

    @GetMapping("/internal/orders/{orderId}")
    fun find(@PathVariable orderId: UUID): OrderRecord? = orders.find(orderId)
}
