package org.kurron.hard.parts.orchestrator

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.shared.FailureStage
import org.kurron.hard.parts.shared.PurchaseRequest
import org.kurron.hard.parts.shared.WorkflowStatus
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class EpicSagaIntegrationTests {

    private lateinit var orderServer: MockWebServer
    private lateinit var paymentServer: MockWebServer
    private lateinit var fulfillmentServer: MockWebServer
    private lateinit var emailServer: MockWebServer
    private lateinit var service: EpicSagaService

    @BeforeEach
    fun setup() {
        orderServer = MockWebServer().apply { start() }
        paymentServer = MockWebServer().apply { start() }
        fulfillmentServer = MockWebServer().apply { start() }
        emailServer = MockWebServer().apply { start() }

        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(1_000)
            setReadTimeout(1_000)
        }

        service = EpicSagaService(
            store = InMemoryWorkflowStore(),
            restClient = RestClient.builder().requestFactory(requestFactory).build(),
            properties = OrchestratorProperties(
                orderUrl = orderServer.url("/").toString().removeSuffix("/"),
                paymentUrl = paymentServer.url("/").toString().removeSuffix("/"),
                fulfillmentUrl = fulfillmentServer.url("/").toString().removeSuffix("/"),
                emailUrl = emailServer.url("/").toString().removeSuffix("/"),
                retryMaxAttempts = 2
            )
        )
    }

    @AfterEach
    fun tearDown() {
        orderServer.shutdown()
        paymentServer.shutdown()
        fulfillmentServer.shutdown()
        emailServer.shutdown()
    }

    @Test
    fun `happy path reaches EMAIL_OK`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val result = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-1", BigDecimal.TEN, "idem-happy")
        )

        assertEquals(WorkflowStatus.EMAIL_OK, result.status)
    }

    @Test
    fun `service down marks workflow failed`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(MockResponse().setResponseCode(500))
        paymentServer.enqueue(MockResponse().setResponseCode(500))

        val result = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-2", BigDecimal.ONE, "idem-down")
        )

        assertEquals(WorkflowStatus.FAILED, result.status)
        assertTrue(result.lastError!!.isNotBlank())
    }

    @Test
    fun `fulfillment failure triggers compensation`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(failed(orderId, correlationId, "no stock"))
        paymentServer.enqueue(ok(orderId, correlationId))

        val result = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-3", BigDecimal.TEN, "idem-comp", FailureStage.FULFILLMENT)
        )

        assertEquals(WorkflowStatus.COMPENSATED, result.status)
    }

    @Test
    fun `resume continues from failed step`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(MockResponse().setResponseCode(500))
        fulfillmentServer.enqueue(MockResponse().setResponseCode(500))

        val first = service.purchase(
            PurchaseRequest(orderId, "customer@example.com", "SKU-4", BigDecimal.TEN, "idem-resume")
        )
        assertEquals(WorkflowStatus.FAILED, first.status)

        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val resumed = service.resume(
            first.workflowId,
            PurchaseRequest(orderId, "customer@example.com", "SKU-4", BigDecimal.TEN, "idem-resume")
        )

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
}

private class InMemoryWorkflowStore : WorkflowStore {

    private val backing = ConcurrentHashMap<Long, WorkflowState>()
    private var sequence = 0L

    override fun findByWorkflowId(workflowId: UUID): WorkflowState? =
        backing.values.firstOrNull { it.workflowId == workflowId }

    override fun findByIdempotencyKey(idempotencyKey: String): WorkflowState? =
        backing.values.firstOrNull { it.idempotencyKey == idempotencyKey }

    override fun save(state: WorkflowState): WorkflowState {
        val id = state.id ?: ++sequence
        val updated = state.copy(id = id)
        backing[id] = updated
        return updated
    }
}
