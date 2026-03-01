package org.kurron.hard.parts.orchestrator

import org.kurron.hard.parts.shared.PurchaseRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/epic")
class EpicSagaController(
    private val service: EpicSagaService
) {

    @PostMapping("/purchase")
    fun purchase(@RequestBody request: PurchaseRequest): WorkflowState {
        return service.purchase(request)
    }

    @PostMapping("/purchase/{workflowId}/resume")
    fun resume(@PathVariable workflowId: UUID, @RequestBody(required = false) request: PurchaseRequest?): WorkflowState {
        return service.resume(workflowId, request)
    }
}
