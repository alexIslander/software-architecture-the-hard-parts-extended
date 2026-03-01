# Coverage Contract: Theory -> Branch -> Code -> Test

| Theory | Branch | Code Path | Test Path | Status |
|---|---|---|---|---|
| Epic (sync, orchestrated, atomic) happy flow | `epic-saga` | `orchestrator/src/main/kotlin/org/kurron/hard/parts/orchestrator/EpicSagaService.kt` | `orchestrator/src/test/kotlin/org/kurron/hard/parts/orchestrator/EpicSagaIntegrationTests.kt` | Implemented |
| Epic compensation for fulfillment failure | `epic-saga` | `EpicSagaService.kt` (`compensatePayment`) | `EpicSagaIntegrationTests.kt` (`fulfillment failure triggers compensation`) | Implemented |
| Epic resume/recovery | `epic-saga` | `EpicSagaService.kt` (`resume`) | `EpicSagaIntegrationTests.kt` (`resume continues from failed step`) | Implemented |
| Anthology async choreography topics/contracts | `anthology-saga` | `saga-shared/src/main/kotlin/org/kurron/hard/parts/shared/SagaContracts.kt` | `saga-shared/src/test/kotlin/org/kurron/hard/parts/shared/EventSchemaConformanceTests.kt` | Implemented |
| Anthology dedup/out-of-order/delay handling | `anthology-saga` | `order-placement/src/main/kotlin/org/kurron/hard/parts/order/core/OrderDomainService.kt` | `order-placement/src/test/kotlin/org/kurron/hard/parts/order/OrderChoreographyTests.kt` | Implemented |
| Contract governance (JSON schema) | `epic-saga`, `anthology-saga` | `saga-shared/src/main/resources/contracts/schemas/*.json` | `EventSchemaConformanceTests.kt`, `payment/.../PaymentContractTests.kt` | Implemented |
| Architecture fitness (module dependency boundary) | all | `order-placement` package boundaries | `order-placement/src/test/kotlin/org/kurron/hard/parts/order/ArchitectureFitnessTests.kt` | Implemented |
| Stamp coupling anti-example | docs | `docs/0301/07-stamp-coupling.md` | `saga-shared/src/test/kotlin/org/kurron/hard/parts/shared/StampCouplingTests.kt` | Implemented |
