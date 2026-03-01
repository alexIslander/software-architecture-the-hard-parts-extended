# Implementation Tasks

Date: 2026-03-01
Scope: follow-up execution before Java 25 / Spring Boot 4 migration.

## Branch-by-Branch Task Board
| Branch | Required Outcome | Status | Evidence |
|---|---|---|---|
| `main` | Stable baseline with runnable tests on Java 21 | In Progress | `mvn clean verify` green on 2026-03-01 |
| `epic-saga` | Full orchestrated implementation + happy/failure/recovery tests + runbook | Implemented in baseline code, branch migration pending | `orchestrator/` module + `EpicSagaIntegrationTests.kt` |
| `anthology-saga` | Full choreography implementation + duplicate/out-of-order tests + runbook | Implemented in baseline code, branch migration pending | async handlers across services + choreography tests |
| `phone-tag-saga` | Full sync choreographed implementation + tests + runbook | Planned | placeholder branch only |
| `parallel-saga` | Full async orchestrated implementation + tests + runbook | Planned | placeholder branch only |
| `fairy-tale-saga` | Full sync orchestrated eventual implementation + tests + runbook | Planned | placeholder branch only |
| `time-travel-saga` | Full sync choreographed eventual implementation + tests + runbook | Planned | placeholder branch only |
| `fantasy-fiction-saga` | Full async orchestrated atomic implementation + tests + runbook | Planned | placeholder branch only |
| `horror-story-saga` | Full async choreographed atomic implementation + tests + runbook | Planned | placeholder branch only |

## Implementation Checklist
1. Baseline stabilization (`main`)
- [x] Keep Java 21 baseline and pass full build gate (`mvn clean verify`).
- [x] Keep migration tasks out of this phase.

2. Epic and anthology implementation hardening
- [x] Orchestrator flow with state persistence, compensation, retry, and resume.
- [x] Async choreography handlers with deduplication and compensation hooks.
- [x] Added module-local tests for fulfillment and email behavior.

3. Test strategy execution
- [x] Build gate: `mvn clean verify`.
- [x] Unit/component tests: core logic and idempotency.
- [x] Integration tests: happy/failure/recovery where implemented.
- [x] Contract tests: JSON schema conformance and service-result contract.
- [x] Architecture fitness tests: boundary checks.

4. Documentation and traceability
- [x] Update theory-to-code matrix to factual `Implemented` vs `Planned`.
- [x] Add branch testing guides and index references.
- [x] Add commit/tag traceability index template for follow-up history.

5. Final migration gate
- [ ] Java 25 + Spring Boot 4 migration.
- [ ] Re-run full branch validation after migration.
