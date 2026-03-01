package org.kurron.hard.parts.orchestrator

import org.kurron.hard.parts.shared.WorkflowStatus
import java.time.Instant
import java.util.UUID

data class ParallelWorkflowState(
    val workflowId: UUID,
    val orderId: UUID,
    val correlationId: String,
    val idempotencyKey: String,
    val status: WorkflowStatus,
    val orderPlaced: Boolean,
    val paymentDone: Boolean,
    val fulfillmentDone: Boolean,
    val emailDone: Boolean,
    val lastError: String?,
    val updatedAt: Instant
)

interface ParallelWorkflowStore {
    fun findByWorkflowId(workflowId: UUID): ParallelWorkflowState?
    fun findByIdempotencyKey(idempotencyKey: String): ParallelWorkflowState?
    fun save(state: ParallelWorkflowState): ParallelWorkflowState
}
