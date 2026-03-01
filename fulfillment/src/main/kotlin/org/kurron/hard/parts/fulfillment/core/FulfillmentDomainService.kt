package org.kurron.hard.parts.fulfillment.core

import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.FulfillmentCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class FulfillmentDomainService {

    private val reservations = ConcurrentHashMap<UUID, String>()
    private val idempotency = ConcurrentHashMap<String, ServiceResult>()
    private val processedEvents = ConcurrentHashMap.newKeySet<String>()

    fun reserve(command: FulfillmentCommand): ServiceResult {
        idempotency[command.idempotencyKey]?.let { return it }
        val result = if (command.failRequest) {
            ServiceResult(command.orderId, command.correlationId, false, "inventory unavailable")
        } else {
            reservations[command.orderId] = "RESERVED"
            ServiceResult(command.orderId, command.correlationId, true, "inventory reserved")
        }
        idempotency[command.idempotencyKey] = result
        return result
    }

    fun cancel(command: FulfillmentCommand): ServiceResult {
        reservations[command.orderId] = "CANCELLED"
        return ServiceResult(command.orderId, command.correlationId, true, "reservation cancelled")
    }

    fun complete(orderId: UUID) {
        reservations[orderId] = "COMPLETED"
    }

    fun status(orderId: UUID): String = reservations[orderId] ?: "NONE"

    fun markProcessedEvent(eventId: String): Boolean = processedEvents.add(eventId)

    fun compensationFromFailure(event: FailureEvent): ServiceResult? {
        if (!processedEvents.add(event.eventId)) {
            return null
        }
        return if (event.stage == FailureStage.EMAIL) {
            cancel(FulfillmentCommand(event.orderId, "N/A", event.correlationId, "failure-${event.eventId}"))
        } else {
            null
        }
    }
}
