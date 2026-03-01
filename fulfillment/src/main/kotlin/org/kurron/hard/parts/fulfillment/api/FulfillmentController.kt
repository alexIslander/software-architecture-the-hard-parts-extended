package org.kurron.hard.parts.fulfillment.api

import org.kurron.hard.parts.fulfillment.core.FulfillmentDomainService
import org.kurron.hard.parts.shared.FulfillmentCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/fulfillment")
class FulfillmentController(
    private val service: FulfillmentDomainService
) {

    @PostMapping("/reserve")
    fun reserve(@RequestBody command: FulfillmentCommand): ServiceResult = service.reserve(command)

    @PostMapping("/cancel")
    fun cancel(@RequestBody command: FulfillmentCommand): ServiceResult = service.cancel(command)

    @GetMapping("/{orderId}")
    fun status(@PathVariable orderId: UUID): String = service.status(orderId)
}
