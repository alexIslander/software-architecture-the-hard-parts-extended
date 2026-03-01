package org.kurron.hard.parts.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrchestrationApplication

fun main(args: Array<String>) {
    runApplication<OrchestrationApplication>(*args)
}
