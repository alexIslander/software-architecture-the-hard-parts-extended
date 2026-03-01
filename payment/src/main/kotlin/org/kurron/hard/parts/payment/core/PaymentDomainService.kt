package org.kurron.hard.parts.payment.core

import org.kurron.hard.parts.shared.FailureEvent
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.PaymentCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class PaymentDomainService {

    private val chargesByOrder = ConcurrentHashMap<UUID, String>()
    private val idempotency = ConcurrentHashMap<String, ServiceResult>()
    private val processedEvents = ConcurrentHashMap.newKeySet<String>()

    fun charge(command: PaymentCommand): ServiceResult {
        idempotency[command.idempotencyKey]?.let { return it }
        val result = if (command.failRequest) {
            ServiceResult(command.orderId, command.correlationId, false, "payment declined")
        } else {
            chargesByOrder[command.orderId] = "CHARGED"
            ServiceResult(command.orderId, command.correlationId, true, "payment processed")
        }
        idempotency[command.idempotencyKey] = result
        return result
    }

    fun refund(command: PaymentCommand): ServiceResult {
        chargesByOrder[command.orderId] = "REFUNDED"
        return ServiceResult(command.orderId, command.correlationId, true, "payment refunded")
    }

    fun status(orderId: UUID): String = chargesByOrder[orderId] ?: "NONE"

    fun markProcessedEvent(eventId: String): Boolean = processedEvents.add(eventId)

    fun compensationFromFailure(event: FailureEvent): ServiceResult? {
        if (!processedEvents.add(event.eventId)) {
            return null
        }
        return if (event.stage == FailureStage.FULFILLMENT || event.stage == FailureStage.EMAIL) {
            refund(PaymentCommand(event.orderId, java.math.BigDecimal.ZERO, event.correlationId, "failure-${event.eventId}"))
        } else {
            null
        }
    }
}
