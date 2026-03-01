package org.kurron.hard.parts.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.shared.ServiceResult
import java.util.UUID

class PaymentContractTests {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)

    @Test
    fun `service result contract stays backward compatible`() {
        val result = ServiceResult(UUID.randomUUID(), "corr", true, "ok")
        val schema = schemaFactory.getSchema(
            this::class.java.getResourceAsStream("/contracts/schemas/service-result-v1.json")
        )
        val errors = schema.validate(objectMapper.readTree(objectMapper.writeValueAsString(result)))
        assertTrue(errors.isEmpty(), "schema validation errors: $errors")
    }
}
