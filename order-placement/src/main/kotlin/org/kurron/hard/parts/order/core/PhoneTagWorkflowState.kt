package org.kurron.hard.parts.order.core

import org.kurron.hard.parts.shared.WorkflowStatus
import java.time.Instant
import java.util.UUID

data class PhoneTagWorkflowState(
    val workflowId: UUID,
    val orderId: UUID,
    val correlationId: String,
    val idempotencyKey: String,
    val status: WorkflowStatus,
    val lastError: String?,
    val updatedAt: Instant
)

interface PhoneTagWorkflowStore {
    fun findByWorkflowId(workflowId: UUID): PhoneTagWorkflowState?
    fun findByIdempotencyKey(idempotencyKey: String): PhoneTagWorkflowState?
    fun save(state: PhoneTagWorkflowState): PhoneTagWorkflowState
}
