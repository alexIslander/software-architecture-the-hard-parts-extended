package org.kurron.hard.parts.order.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "phone-tag")
data class PhoneTagSagaProperties(
    val paymentUrl: String = "http://localhost:8082",
    val fulfillmentUrl: String = "http://localhost:8083",
    val emailUrl: String = "http://localhost:8084",
    val retryMaxAttempts: Int = 3
)
