# Branch Testing Guides

Date: 2026-03-01
Scope: test execution before Java 25 / Spring Boot 4 migration.

## epic-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: `docker compose up -d`.
- App startup commands:
  - `mvn -pl orchestrator spring-boot:run`
  - `mvn -pl order-placement spring-boot:run`
  - `mvn -pl payment spring-boot:run`
  - `mvn -pl fulfillment spring-boot:run`
  - `mvn -pl email spring-boot:run`
- Happy-path test command: `mvn -pl orchestrator test -Dtest=EpicSagaIntegrationTests`.
- Failure-path test command: `mvn -pl orchestrator test -Dtest=EpicSagaIntegrationTests`.
- Resume/recovery command: `mvn -pl orchestrator test -Dtest=EpicSagaIntegrationTests`.
- Teardown command: `docker compose down`.
- Expected outputs: orchestrator workflow reaches `EMAIL_OK` on happy path and `FAILED`/`COMPENSATED` on failure scenarios.

## anthology-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: `docker compose up -d`.
- App startup commands:
  - `mvn -pl order-placement spring-boot:run`
  - `mvn -pl payment spring-boot:run`
  - `mvn -pl fulfillment spring-boot:run`
  - `mvn -pl email spring-boot:run`
- Happy-path test command: `mvn -pl order-placement test -Dtest=OrderChoreographyTests,AnthologySagaProjectionTests`.
- Failure-path test command: `mvn -pl order-placement test -Dtest=AnthologySagaProjectionTests#failure\\ event\\ marks\\ projection\\ and\\ duplicate\\ is\\ ignored`.
- Resume/recovery command: `mvn -pl order-placement test -Dtest=AnthologySagaProjectionTests#projection\\ keeps\\ pending\\ count\\ until\\ dependencies\\ arrive`.
- Teardown command: `docker compose down`.
- Expected outputs: duplicate events are ignored, out-of-order choreography events reconcile eventually, and projection endpoints expose pending and final status transitions without centralized orchestration.

## phone-tag-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.

## parallel-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.

## fairy-tale-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.

## time-travel-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.

## fantasy-fiction-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.

## horror-story-saga
- Prerequisites: JDK 21+, Docker running.
- Infra startup command: Planned.
- App startup commands: Planned.
- Happy-path test command: Planned.
- Failure-path test command: Planned.
- Resume/recovery command: Planned.
- Teardown command: Planned.
- Expected outputs: Planned.
