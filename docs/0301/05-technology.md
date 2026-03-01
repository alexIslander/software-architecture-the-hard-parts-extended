# Technology and Dependency Plan (Implemented)

## Mandatory Upgrade (Completed First)
Applied before feature work:
- Java: `21`
- Spring Boot: upgraded `3.3.4 -> 3.5.10`
- Kotlin: upgraded `1.9.25 -> 2.1.21`
- Messaging runtime: `Pulsar -> RabbitMQ`

## Implemented Stack
- Language: Kotlin
- Build: Maven multi-module
- Framework: Spring Boot 3.5.x
- Sync workflows (Epic): REST/HTTP (`RestClient`)
- Async workflows (Anthology): RabbitMQ topic exchange
- Workflow persistence: Spring Data JDBC + Liquibase (orchestrator)
- Resilience: Resilience4j (`resilience4j-spring-boot3`)
- Testing: JUnit 5 + schema and choreography tests + orchestrator scenario tests
- Architecture fitness: ArchUnit
- Contract governance: JSON Schema + compatibility/conformance tests

## Key Dependencies in Use
- `spring-boot-starter-web`
- `spring-boot-starter-data-jdbc`
- `spring-boot-starter-amqp`
- `spring-boot-starter-actuator`
- `liquibase-core`
- `postgresql`
- `resilience4j-spring-boot3`
- `spring-boot-starter-test`
- `archunit-junit5`
- `json-schema-validator`
- `mockwebserver`

## CI Gates
- `mvn verify` on push/PR (`.github/workflows/ci.yml`)
- Includes tests, architecture rules, and contract schema checks.
