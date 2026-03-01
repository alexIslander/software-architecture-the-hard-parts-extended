package org.kurron.hard.parts.order.core

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryPhoneTagWorkflowStore : PhoneTagWorkflowStore {

    private val byWorkflowId = ConcurrentHashMap<UUID, PhoneTagWorkflowState>()
    private val byIdempotency = ConcurrentHashMap<String, UUID>()

    override fun findByWorkflowId(workflowId: UUID): PhoneTagWorkflowState? = byWorkflowId[workflowId]

    override fun findByIdempotencyKey(idempotencyKey: String): PhoneTagWorkflowState? {
        val workflowId = byIdempotency[idempotencyKey] ?: return null
        return byWorkflowId[workflowId]
    }

    override fun save(state: PhoneTagWorkflowState): PhoneTagWorkflowState {
        byWorkflowId[state.workflowId] = state
        byIdempotency[state.idempotencyKey] = state.workflowId
        return state
    }
}
