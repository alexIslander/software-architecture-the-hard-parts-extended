# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-module Maven project for saga-pattern examples in Kotlin/Spring Boot.

- Root: `pom.xml` (aggregator), `compose.yaml` (PostgreSQL + Pulsar), `README.adoc`, `architectural-diagrams/`.
- Services: `order-placement/`, `payment/`, `fulfillment/`, `email/`.
- Per service: `src/main/kotlin/` for application code, `src/main/resources/` for config, `src/test/kotlin/` for tests, and optional local `compose.yaml`.

Package naming follows `org.kurron.hard.parts.<service>`.

## Build, Test, and Development Commands
- `mvn clean verify`: compile and run all tests across modules.
- `mvn clean package -DskipTests`: build artifacts without tests.
- `mvn -pl order-placement spring-boot:run`: run one service locally (swap module name as needed).
- `docker compose up -d`: start shared local dependencies (PostgreSQL and Pulsar) from repo root.
- `mvn -pl payment test`: run tests for a single module.

Use JDK 21+ before running commands.

## Coding Style & Naming Conventions
- Language: Kotlin (Spring Boot 3.x).
- Follow Kotlin/IntelliJ defaults for formatting and imports.
- Use `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields, and clear service-specific package names.
- Keep configuration in `application.properties` under each module.
- Prefer small, focused Spring components over large multipurpose classes.

## Testing Guidelines
- Frameworks: JUnit 5 + Spring Boot Test + Testcontainers.
- Put tests in each module’s `src/test/kotlin`.
- Name integration/context tests with `*ApplicationTests` and helper bootstrap files with `Test*Application` or `*TestcontainersConfiguration`.
- Run `mvn test` (all modules) or `mvn -pl <module> test` (single module) before opening a PR.

## Commit & Pull Request Guidelines
Recent history uses short, imperative messages (for example, `Added payment service`, `Fix table`).

- Commit messages: imperative mood, specific scope, ~72 chars max subject.
- Keep commits focused to one concern.
- PRs should include: purpose, impacted module(s), local test commands run, and any architecture/diagram updates when behavior changes.
- Link the related issue or branch context for the saga variant being changed.
