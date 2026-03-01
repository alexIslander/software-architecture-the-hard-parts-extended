package org.kurron.hard.parts.order.api

import org.kurron.hard.parts.order.core.PhoneTagSagaService
import org.kurron.hard.parts.order.core.PhoneTagWorkflowState
import org.kurron.hard.parts.shared.PurchaseRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/phone-tag")
class PhoneTagSagaController(
    private val service: PhoneTagSagaService
) {

    @PostMapping("/purchase")
    fun purchase(@RequestBody request: PurchaseRequest): PhoneTagWorkflowState = service.purchase(request)

    @PostMapping("/purchase/{workflowId}/resume")
    fun resume(
        @PathVariable workflowId: UUID,
        @RequestBody(required = false) request: PurchaseRequest?
    ): PhoneTagWorkflowState = service.resume(workflowId, request)
}
