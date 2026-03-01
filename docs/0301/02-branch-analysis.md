# Branch Analysis

## How Evidence Was Collected
- `git branch -a` for available branch set.
- `git ls-tree -r --name-only <branch>` for artifact inventory.
- `git show <branch>:<path>` for file-level verification.
- `git diff --name-only <a>..<b> -- . ':(exclude)architectural-diagrams/**'` for code delta checks.

## Cross-Branch Findings
1. `phone-tag-saga`, `parallel-saga`, and `anthology-saga` are effectively identical in code (outside `architectural-diagrams`).
2. `epic-saga` is the only branch with additional modules and testing surface (`orchestrator`, `specification-by-example`).
3. In all saga branches, service runtime code is mostly bootstrap-only (Spring app entrypoints).
4. Main branch currently contains a simplified Structurizr template in `architectural-diagrams/workspace.dsl`, not the saga-specific models.

## Branch Details

### `epic-saga`
- Modules: `orchestrator`, `order-placement`, `payment`, `fulfillment`, `email`, `specification-by-example`.
- Architecture intent: orchestrator-centric sync flow and atomic consistency (from `workspace.dsl`, ADR `0002-microservices-architecture.md`).
- Code reality:
  - `orchestrator/src/main/.../OrchestrationApplication.kt` is bootstrap-only.
  - `specification-by-example` feature file exists, but Java step definitions only log.
- Net: **best branch for learning intent**, but **not a full executable saga**.

### `phone-tag-saga`
- Modules: 4 core services only.
- Architecture intent: choreographed synchronous chain (`Front Controller -> Order -> Payment -> Fulfillment -> E-mail`) in `workspace.dsl`.
- ADRs discuss routing/modulith choices, but code does not implement those message flows.
- Net: **diagram-first representation**.

### `parallel-saga`
- Modules: 4 core services only.
- Architecture intent: orchestrated, asynchronous (`JSON over AMQP`) and eventual consistency in `workspace.dsl`.
- No orchestrator module or AMQP handler implementation found.
- Net: **concept modeled, runtime missing**.

### `anthology-saga`
- Modules: 4 core services only.
- Architecture intent: choreographed asynchronous chain via AMQP in `workspace.dsl`.
- No event-driven choreography handlers in code.
- Net: **concept modeled, runtime missing**.

### `main`
- 4 core modules + placeholder-like `workspace.dsl`.
- Good as baseline project structure, not as saga branch evidence.

## Coverage vs Book's 8 Saga Types
README lists 8 patterns, but branch evidence exists for 4:
- Present: `epic`, `phone-tag`, `parallel`, `anthology`
- Missing branches/implementations: `fairy-tale`, `time-travel`, `fantasy-fiction`, `horror-story`

## Repro Commands
```bash
git ls-tree -r --name-only epic-saga | rg 'orchestrator|specification-by-example'
git diff --name-only phone-tag-saga..parallel-saga -- . ':(exclude)architectural-diagrams/**'
git show anthology-saga:architectural-diagrams/workspace.dsl
git show epic-saga:specification-by-example/src/test/resources/features/purchase-item.feature
```
