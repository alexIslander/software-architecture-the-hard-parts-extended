package org.kurron.hard.parts.orchestrator

import org.kurron.hard.parts.shared.WorkflowStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("WORKFLOW_STATE")
data class WorkflowState(
    @Id
    val id: Long? = null,
    val workflowId: UUID,
    val orderId: UUID,
    val correlationId: String,
    val idempotencyKey: String,
    val status: WorkflowStatus,
    val lastError: String?,
    val updatedAt: Instant
)
