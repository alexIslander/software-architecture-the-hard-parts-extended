package org.kurron.hard.parts.orchestrator

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "orchestrator")
data class OrchestratorProperties(
    val orderUrl: String,
    val paymentUrl: String,
    val fulfillmentUrl: String,
    val emailUrl: String,
    val retryMaxAttempts: Int = 3
)
