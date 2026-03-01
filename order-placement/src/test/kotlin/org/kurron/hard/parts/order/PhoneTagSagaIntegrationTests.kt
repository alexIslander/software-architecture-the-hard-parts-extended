package org.kurron.hard.parts.order

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.order.config.PhoneTagSagaProperties
import org.kurron.hard.parts.order.core.OrderDomainService
import org.kurron.hard.parts.order.core.PhoneTagSagaService
import org.kurron.hard.parts.order.core.PhoneTagWorkflowState
import org.kurron.hard.parts.order.core.PhoneTagWorkflowStore
import org.kurron.hard.parts.shared.PurchaseRequest
import org.kurron.hard.parts.shared.WorkflowStatus
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PhoneTagSagaIntegrationTests {

    private lateinit var paymentServer: MockWebServer
    private lateinit var fulfillmentServer: MockWebServer
    private lateinit var emailServer: MockWebServer
    private lateinit var service: PhoneTagSagaService

    @BeforeEach
    fun setup() {
        paymentServer = MockWebServer().apply { start() }
        fulfillmentServer = MockWebServer().apply { start() }
        emailServer = MockWebServer().apply { start() }

        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(1_000)
            setReadTimeout(1_000)
        }

        service = PhoneTagSagaService(
            orders = OrderDomainService(),
            store = InMemoryPhoneTagStore(),
            restClient = RestClient.builder().requestFactory(requestFactory).build(),
            properties = PhoneTagSagaProperties(
                paymentUrl = paymentServer.url("/").toString().removeSuffix("/"),
                fulfillmentUrl = fulfillmentServer.url("/").toString().removeSuffix("/"),
                emailUrl = emailServer.url("/").toString().removeSuffix("/"),
                retryMaxAttempts = 2
            )
        )
    }

    @AfterEach
    fun tearDown() {
        paymentServer.shutdown()
        fulfillmentServer.shutdown()
        emailServer.shutdown()
    }

    @Test
    fun `happy path reaches EMAIL_OK`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val result = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-PT-1", BigDecimal.TEN, "pt-happy")
        )

        assertEquals(WorkflowStatus.EMAIL_OK, result.status)
    }

    @Test
    fun `fulfillment failure triggers payment compensation`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(failed(orderId, correlationId, "inventory unavailable"))
        paymentServer.enqueue(ok(orderId, correlationId))

        val result = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-PT-2", BigDecimal.TEN, "pt-compensate")
        )

        assertEquals(WorkflowStatus.COMPENSATED, result.status)
        assertEquals(2, paymentServer.requestCount)
    }

    @Test
    fun `resume recovers after transient payment outage`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        paymentServer.enqueue(MockResponse().setResponseCode(500))
        paymentServer.enqueue(MockResponse().setResponseCode(500))

        val request = PurchaseRequest(orderId, "customer@example.com", "SKU-PT-3", BigDecimal.TEN, "pt-resume")
        val first = service.purchase(request)

        assertEquals(WorkflowStatus.FAILED, first.status)

        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val resumed = service.resume(first.workflowId, request)

        assertEquals(WorkflowStatus.EMAIL_OK, resumed.status)
    }

    private fun ok(orderId: UUID, correlationId: String): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"orderId":"$orderId","correlationId":"$correlationId","success":true,"details":"ok"}""")
    }

    private fun failed(orderId: UUID, correlationId: String, details: String): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"orderId":"$orderId","correlationId":"$correlationId","success":false,"details":"$details"}""")
    }

    private class InMemoryPhoneTagStore : PhoneTagWorkflowStore {
        private val byWorkflowId = ConcurrentHashMap<UUID, PhoneTagWorkflowState>()
        private val byIdempotency = ConcurrentHashMap<String, UUID>()

        override fun findByWorkflowId(workflowId: UUID): PhoneTagWorkflowState? = byWorkflowId[workflowId]

        override fun findByIdempotencyKey(idempotencyKey: String): PhoneTagWorkflowState? {
            val workflowId = byIdempotency[idempotencyKey] ?: return null
            return byWorkflowId[workflowId]
        }

        override fun save(state: PhoneTagWorkflowState): PhoneTagWorkflowState {
            val updated = state.copy(updatedAt = Instant.now())
            byWorkflowId[updated.workflowId] = updated
            byIdempotency[updated.idempotencyKey] = updated.workflowId
            return updated
        }
    }
}
