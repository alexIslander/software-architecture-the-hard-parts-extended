package org.kurron.hard.parts.order.core

import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.OrderRecord
import org.kurron.hard.parts.shared.SagaEvent
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderDomainService {

    private val orders = ConcurrentHashMap<UUID, OrderRecord>()
    private val processedEventIds = ConcurrentHashMap.newKeySet<String>()
    private val pendingEvents = ConcurrentHashMap<UUID, MutableList<SagaEvent>>()

    fun create(order: OrderRecord): OrderRecord {
        return orders.computeIfAbsent(order.orderId) { order }
    }

    fun markStatus(orderId: UUID, status: String): OrderRecord? {
        val existing = orders[orderId] ?: return null
        val updated = existing.copy(status = status)
        orders[orderId] = updated
        return updated
    }

    fun find(orderId: UUID): OrderRecord? = orders[orderId]

    fun applyEvent(event: SagaEvent) {
        if (!processedEventIds.add(event.eventId)) {
            return
        }
        when (event.eventType) {
            "payment.processed" -> markStatus(event.orderId, "PAYMENT_OK")
            "fulfillment.completed" -> {
                if (orders[event.orderId]?.status == "PAYMENT_OK") {
                    markStatus(event.orderId, "FULFILLMENT_OK")
                } else {
                    pendingEvents.computeIfAbsent(event.orderId) { mutableListOf() }.add(event)
                }
            }
            "email.sent" -> markStatus(event.orderId, "EMAIL_OK")
        }
    }

    fun applyFailure(event: FailureEvent) {
        if (!processedEventIds.add(event.eventId)) {
            return
        }
        markStatus(event.orderId, "FAILED:${event.stage}")
    }

    fun reconcile() {
        pendingEvents.forEach { (orderId, events) ->
            if (orders[orderId]?.status == "PAYMENT_OK") {
                events.forEach { markStatus(orderId, "FULFILLMENT_OK") }
                pendingEvents.remove(orderId)
            }
        }
    }
}
