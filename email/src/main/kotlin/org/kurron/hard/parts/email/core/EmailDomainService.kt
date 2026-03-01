package org.kurron.hard.parts.email.core

import org.kurron.hard.parts.shared.EmailCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class EmailDomainService {

    private val sent = ConcurrentHashMap<UUID, String>()
    private val idempotency = ConcurrentHashMap<String, ServiceResult>()
    private val processedEvents = ConcurrentHashMap.newKeySet<String>()

    fun send(command: EmailCommand): ServiceResult {
        idempotency[command.idempotencyKey]?.let { return it }
        val result = if (command.failRequest) {
            ServiceResult(command.orderId, command.correlationId, false, "smtp timeout")
        } else {
            sent[command.orderId] = "SENT:${command.customerEmail}"
            ServiceResult(command.orderId, command.correlationId, true, "email sent")
        }
        idempotency[command.idempotencyKey] = result
        return result
    }

    fun status(orderId: UUID): String = sent[orderId] ?: "NONE"

    fun markProcessedEvent(eventId: String): Boolean = processedEvents.add(eventId)
}
