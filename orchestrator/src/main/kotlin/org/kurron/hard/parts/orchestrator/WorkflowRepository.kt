package org.kurron.hard.parts.orchestrator

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface WorkflowRepository : CrudRepository<WorkflowState, Long> {
    fun findByWorkflowId(workflowId: UUID): WorkflowState?
    fun findByIdempotencyKey(idempotencyKey: String): WorkflowState?
}
