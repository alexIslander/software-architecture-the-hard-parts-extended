package org.kurron.hard.parts.orchestrator

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kurron.hard.parts.shared.PurchaseRequest
import org.kurron.hard.parts.shared.WorkflowStatus
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ParallelSagaIntegrationTests {

    private lateinit var orderServer: MockWebServer
    private lateinit var paymentServer: MockWebServer
    private lateinit var fulfillmentServer: MockWebServer
    private lateinit var emailServer: MockWebServer
    private lateinit var service: ParallelSagaService

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

        service = ParallelSagaService(
            store = InMemoryParallelStore(),
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
    fun `parallel advancement reaches EMAIL_OK without compensation`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val request = PurchaseRequest(orderId, "customer@example.com", "SKU-P-1", BigDecimal.TEN, "parallel-happy")
        val started = service.purchase(request)
        assertEquals(WorkflowStatus.STARTED, started.status)

        val step1 = service.advance(started.workflowId, request)
        assertEquals(WorkflowStatus.ORDER_OK, step1.status)
        val step2 = service.advance(started.workflowId, request)
        assertEquals(WorkflowStatus.FULFILLMENT_OK, step2.status)
        val step3 = service.advance(started.workflowId, request)
        assertEquals(WorkflowStatus.EMAIL_OK, step3.status)

        assertEquals(1, paymentServer.requestCount)
        assertEquals(1, fulfillmentServer.requestCount)
    }

    @Test
    fun `partial parallel failure keeps successful branch and resumes eventually`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(failed(orderId, correlationId, "inventory lag"))

        val request = PurchaseRequest(orderId, "customer@example.com", "SKU-P-2", BigDecimal.TEN, "parallel-resume")
        val started = service.purchase(request)
        service.advance(started.workflowId, request)
        val failedState = service.advance(started.workflowId, request)

        assertEquals(WorkflowStatus.FAILED, failedState.status)
        assertTrue(failedState.paymentDone)
        assertTrue(!failedState.fulfillmentDone)
        assertEquals(1, paymentServer.requestCount)

        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val resumed = service.resume(started.workflowId, request)
        assertEquals(WorkflowStatus.FULFILLMENT_OK, resumed.status)
        val completed = service.advance(started.workflowId, request)
        assertEquals(WorkflowStatus.EMAIL_OK, completed.status)
        assertEquals(1, paymentServer.requestCount)
    }

    @Test
    fun `transient parallel stage outage recovers with repeated advance`() {
        val orderId = UUID.randomUUID()
        val correlationId = UUID.randomUUID().toString()
        orderServer.enqueue(ok(orderId, correlationId))
        paymentServer.enqueue(MockResponse().setResponseCode(500))
        paymentServer.enqueue(MockResponse().setResponseCode(500))
        fulfillmentServer.enqueue(MockResponse().setResponseCode(500))
        fulfillmentServer.enqueue(MockResponse().setResponseCode(500))

        val request = PurchaseRequest(orderId, "customer@example.com", "SKU-P-3", BigDecimal.TEN, "parallel-retry")
        val started = service.purchase(request)
        service.advance(started.workflowId, request)
        val failed = service.advance(started.workflowId, request)
        assertEquals(WorkflowStatus.FAILED, failed.status)

        paymentServer.enqueue(ok(orderId, correlationId))
        fulfillmentServer.enqueue(ok(orderId, correlationId))
        emailServer.enqueue(ok(orderId, correlationId))

        val resumed = service.resume(started.workflowId, request)
        assertEquals(WorkflowStatus.FULFILLMENT_OK, resumed.status)
        assertEquals(WorkflowStatus.EMAIL_OK, service.advance(started.workflowId, request).status)
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

private class InMemoryParallelStore : ParallelWorkflowStore {

    private val byWorkflowId = ConcurrentHashMap<UUID, ParallelWorkflowState>()
    private val byIdempotency = ConcurrentHashMap<String, UUID>()

    override fun findByWorkflowId(workflowId: UUID): ParallelWorkflowState? = byWorkflowId[workflowId]

    override fun findByIdempotencyKey(idempotencyKey: String): ParallelWorkflowState? {
        val workflowId = byIdempotency[idempotencyKey] ?: return null
        return byWorkflowId[workflowId]
    }

    override fun save(state: ParallelWorkflowState): ParallelWorkflowState {
        byWorkflowId[state.workflowId] = state
        byIdempotency[state.idempotencyKey] = state.workflowId
        return state
    }
}
