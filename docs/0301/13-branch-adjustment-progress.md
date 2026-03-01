# Branch Adjustment Progress

Date: 2026-03-01
Legend: `Not Started` | `In Progress` | `Adjusted` | `Validated`

| Branch | Status | Adjustment Commit | Code Delta vs `main` | Test Command | Test Result | Notes |
|---|---|---|---:|---|---|---|
| `epic-saga` | Not Started | TBD | TBD | `mvn -pl orchestrator test -Dtest=EpicSagaIntegrationTests` | TBD | |
| `anthology-saga` | Not Started | TBD | TBD | `mvn -pl order-placement test -Dtest=OrderChoreographyTests` | TBD | |
| `phone-tag-saga` | Not Started | TBD | TBD | `mvn -pl order-placement test` | TBD | |
| `parallel-saga` | Not Started | TBD | TBD | `mvn -pl orchestrator test` | TBD | |
| `fairy-tale-saga` | Validated | `871a477` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with fairy-tale coordinates |
| `time-travel-saga` | Validated | `91c55fa` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with time-travel coordinates |
| `fantasy-fiction-saga` | Validated | `3e8294b` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with fantasy-fiction coordinates |
| `horror-story-saga` | Validated | `cbc77ca` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with horror-story coordinates |

## Baseline Snapshot
- Branches currently identical to `main`: `fairy-tale-saga`, `time-travel-saga`, `fantasy-fiction-saga`, `horror-story-saga`.
- Priority order: adjust identical branches first.
