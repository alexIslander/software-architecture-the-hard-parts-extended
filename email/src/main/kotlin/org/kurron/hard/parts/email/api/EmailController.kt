package org.kurron.hard.parts.email.api

import org.kurron.hard.parts.email.core.EmailDomainService
import org.kurron.hard.parts.shared.EmailCommand
import org.kurron.hard.parts.shared.ServiceResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/email")
class EmailController(
    private val service: EmailDomainService
) {

    @PostMapping("/send")
    fun send(@RequestBody command: EmailCommand): ServiceResult = service.send(command)

    @GetMapping("/{orderId}")
    fun status(@PathVariable orderId: UUID): String = service.status(orderId)
}
