# Branch Adjustment Progress

Date: 2026-03-01
Legend: `Not Started` | `In Progress` | `Adjusted` | `Validated`

| Branch | Status | Adjustment Commit | Code Delta vs `main` | Test Command | Test Result | Notes |
|---|---|---|---:|---|---|---|
| `epic-saga` | Validated | `2f5bd23` | 49 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with epic coordinates |
| `anthology-saga` | Validated | `2c667b1` | 42 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with anthology coordinates |
| `phone-tag-saga` | Validated | `ad21ea5` | 42 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with phone-tag coordinates |
| `parallel-saga` | Not Started | TBD | TBD | `mvn -pl orchestrator test` | TBD | |
| `fairy-tale-saga` | Validated | `871a477` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with fairy-tale coordinates |
| `time-travel-saga` | Validated | `91c55fa` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with time-travel coordinates |
| `fantasy-fiction-saga` | Validated | `3e8294b` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with fantasy-fiction coordinates |
| `horror-story-saga` | Validated | `cbc77ca` | 1 | `mvn -pl saga-shared test -Dtest=EventSchemaConformanceTests` | Pass | Added `saga-shared/src/main/resources/saga-implementation.properties` with horror-story coordinates |

## Baseline Snapshot
- Branches currently identical to `main`: `fairy-tale-saga`, `time-travel-saga`, `fantasy-fiction-saga`, `horror-story-saga`.
- Priority order: adjust identical branches first.
