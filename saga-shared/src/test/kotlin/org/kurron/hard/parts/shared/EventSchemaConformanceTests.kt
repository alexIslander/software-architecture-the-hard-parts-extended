package org.kurron.hard.parts.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class EventSchemaConformanceTests {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules().disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)

    @Test
    fun `saga event matches schema`() {
        val payload = SagaEvent(
            eventId = UUID.randomUUID().toString(),
            correlationId = UUID.randomUUID().toString(),
            orderId = UUID.randomUUID(),
            eventType = SagaTopics.ORDER_PLACED,
            createdAt = Instant.now(),
            payload = mapOf("amount" to BigDecimal("11.00"), "itemSku" to "BOOK-1")
        )

        val schema = schemaFactory.getSchema(
            this::class.java.getResourceAsStream("/contracts/schemas/saga-event-v1.json")
        )
        val node = objectMapper.readTree(objectMapper.writeValueAsString(payload))
        val errors = schema.validate(node)

        assertTrue(errors.isEmpty(), "schema validation errors: $errors")
    }

    @Test
    fun `failure event matches schema`() {
        val payload = FailureEvent(
            eventId = UUID.randomUUID().toString(),
            correlationId = UUID.randomUUID().toString(),
            orderId = UUID.randomUUID(),
            stage = FailureStage.PAYMENT,
            reason = "card declined",
            createdAt = Instant.now()
        )

        val schema = schemaFactory.getSchema(
            this::class.java.getResourceAsStream("/contracts/schemas/failure-event-v1.json")
        )
        val node = objectMapper.readTree(objectMapper.writeValueAsString(payload))
        val errors = schema.validate(node)

        assertTrue(errors.isEmpty(), "schema validation errors: $errors")
    }
}
