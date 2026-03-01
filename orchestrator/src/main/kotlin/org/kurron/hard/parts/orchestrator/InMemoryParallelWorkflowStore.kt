package org.kurron.hard.parts.orchestrator

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryParallelWorkflowStore : ParallelWorkflowStore {

    private val byWorkflowId = ConcurrentHashMap<UUID, ParallelWorkflowState>()
    private val byIdempotency = ConcurrentHashMap<String, UUID>()

    override fun findByWorkflowId(workflowId: UUID): ParallelWorkflowState? = byWorkflowId[workflowId]

    override fun findByIdempotencyKey(idempotencyKey: String): ParallelWorkflowState? {
        val workflowId = byIdempotency[idempotencyKey] ?: return null
        return byWorkflowId[workflowId]
    }

    override fun save(state: ParallelWorkflowState): ParallelWorkflowState {
        byWorkflowId[state.workflowId] = state
        byIdempotency[state.idempotencyKey] = state.workflowId
        return state
    }
}
