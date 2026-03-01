# Theory to Code Map

Date: 2026-03-01
Scope: current `main` implementation (Java 21 baseline), before Java 25 migration.

## Theory Mapping Matrix
| Theory | Branch Target | Implementation Evidence | Test Evidence | Status |
|---|---|---|---|---|
| Epic saga orchestration (sync + atomic + orchestrated) | `epic-saga` | `orchestrator/src/main/kotlin/org/kurron/hard/parts/orchestrator/EpicSagaService.kt` | `orchestrator/src/test/kotlin/org/kurron/hard/parts/orchestrator/EpicSagaIntegrationTests.kt` | Implemented on `main` baseline |
| Compensation behavior | `epic-saga` | `EpicSagaService.kt` (`compensatePayment`, `compensateFulfillment`) | `EpicSagaIntegrationTests.kt` (`fulfillment failure triggers compensation`) | Implemented on `main` baseline |
| Idempotency + retry + resume | `epic-saga` | `EpicSagaService.kt` (`purchase`, `postWithRetry`, `resume`) | `EpicSagaIntegrationTests.kt` (`resume continues from failed step`) | Implemented on `main` baseline |
| Anthology choreography (async + eventual + choreographed) | `anthology-saga` | `order-placement/src/main/kotlin/org/kurron/hard/parts/order/async/OrderEventListener.kt`, `payment/.../PaymentEventHandlers.kt`, `fulfillment/.../FulfillmentEventHandlers.kt`, `email/.../EmailEventHandlers.kt` | `order-placement/src/test/kotlin/org/kurron/hard/parts/order/OrderChoreographyTests.kt`, `fulfillment/src/test/kotlin/org/kurron/hard/parts/fulfillment/FulfillmentEventHandlersTests.kt`, `email/src/test/kotlin/org/kurron/hard/parts/email/EmailEventHandlersTests.kt` | Implemented on `main` baseline |
| Deduplication and out-of-order handling | `anthology-saga` | `OrderDomainService.kt`, `PaymentDomainService.kt`, `FulfillmentDomainService.kt`, `EmailDomainService.kt` | `OrderChoreographyTests.kt`, `FulfillmentDomainServiceTests.kt`, `EmailDomainServiceTests.kt` | Implemented on `main` baseline |
| Contract governance + schema conformance | cross-branch | `saga-shared/src/main/resources/contracts/schemas/*.json` | `saga-shared/src/test/kotlin/org/kurron/hard/parts/shared/EventSchemaConformanceTests.kt`, `payment/src/test/kotlin/org/kurron/hard/parts/payment/PaymentContractTests.kt` | Implemented |
| Architecture fitness function | cross-branch | `order-placement` package boundaries | `order-placement/src/test/kotlin/org/kurron/hard/parts/order/ArchitectureFitnessTests.kt` | Implemented |
| Stamp coupling anti-example | cross-branch | `docs/0301/07-stamp-coupling.md` | `saga-shared/src/test/kotlin/org/kurron/hard/parts/shared/StampCouplingTests.kt` | Implemented |
| Fairy Tale saga branch | `fairy-tale-saga` | branch placeholder only | no automated tests in this branch | Planned |
| Time Travel saga branch | `time-travel-saga` | branch placeholder only | no automated tests in this branch | Planned |
| Fantasy Fiction saga branch | `fantasy-fiction-saga` | branch placeholder only | no automated tests in this branch | Planned |
| Horror Story saga branch | `horror-story-saga` | branch placeholder only | no automated tests in this branch | Planned |

## Notes
1. `main` currently carries executable Epic and Anthology examples for shared validation.
2. Follow-up work should move each saga to its dedicated branch with branch-local runbook and full test suite.
3. Java 25 / Spring Boot 4 migration remains explicitly out of scope until all branch implementations are complete and validated.
