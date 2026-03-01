package org.kurron.hard.parts.orchestrator

import org.springframework.stereotype.Component
import java.util.UUID

interface WorkflowStore {
    fun findByWorkflowId(workflowId: UUID): WorkflowState?
    fun findByIdempotencyKey(idempotencyKey: String): WorkflowState?
    fun save(state: WorkflowState): WorkflowState
}

@Component
class WorkflowStoreAdapter(
    private val repository: WorkflowRepository
) : WorkflowStore {
    override fun findByWorkflowId(workflowId: UUID): WorkflowState? = repository.findByWorkflowId(workflowId)
    override fun findByIdempotencyKey(idempotencyKey: String): WorkflowState? = repository.findByIdempotencyKey(idempotencyKey)
    override fun save(state: WorkflowState): WorkflowState = repository.save(state)
}
