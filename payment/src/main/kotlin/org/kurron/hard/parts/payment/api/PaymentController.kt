package org.kurron.hard.parts.payment.api

import org.kurron.hard.parts.payment.core.PaymentDomainService
import org.kurron.hard.parts.shared.PaymentCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/payments")
class PaymentController(
    private val service: PaymentDomainService
) {

    @PostMapping("/charge")
    fun charge(@RequestBody command: PaymentCommand): ServiceResult = service.charge(command)

    @PostMapping("/refund")
    fun refund(@RequestBody command: PaymentCommand): ServiceResult = service.refund(command)

    @GetMapping("/{orderId}")
    fun status(@PathVariable orderId: UUID): String = service.status(orderId)
}
