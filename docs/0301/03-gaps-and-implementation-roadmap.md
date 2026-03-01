# Gaps and Implementation Roadmap

## Critical Gaps (Theory Without Strong Code Examples)

## P0 - Core Saga Mechanics Missing
1. **Compensating transactions** not implemented.
2. **Idempotency and retry semantics** not implemented.
3. **Workflow state persistence/recovery** not implemented (despite orchestration/choreography focus).
4. **End-to-end happy/sad path verification** not implemented (BDD steps only log calls).

Why this matters: these are the "hard parts" of distributed workflows, and they are central in your note and in Chapter 11/12 style discussions.

## P1 - Data and Consistency Patterns Missing
1. No concrete **event-based eventual consistency** flow handlers.
2. No **background synchronization** or **orchestrated request-based consistency** implementation.
3. No **data access pattern** implementation examples (interservice calls vs replicated schema/cache vs data domain).

Why this matters: chapters on data ownership/distributed transactions are not traceable to runnable code.

## P2 - Contract Governance Missing
1. No **consumer-driven contract** tests.
2. No **schema evolution checks** for strict/loose contracts.
3. No explicit **stamp-coupling strategy** in message payloads.

Why this matters: contract brittleness and cross-service breakage are key failure modes in distributed systems.

## KISS Roadmap (Minimal, High-Leverage)

### Step 1: Make one branch executable end-to-end (`epic-saga`)
- Add one purchase command flow with 4 service calls.
- Persist orchestration state with statuses (`STARTED`, `PAYMENT_OK`, etc.).
- Implement one compensation path (e.g., payment success + fulfillment failure => payment revert).
- Upgrade BDD steps to real assertions.

### Step 2: Add one async branch (`anthology-saga`)
- Implement AMQP topic exchange and message contracts.
- Add idempotent consumers with correlation ID.
- Demonstrate eventual consistency + replay-safe processing.

### Step 3: Add contract safety net
- Introduce consumer-driven tests between `order-placement` and `payment`.
- Add schema compatibility checks in CI.

### Step 4: Close taxonomy gap
- Add missing branches or a single comparison doc proving why 4 are intentionally omitted.

## Suggested Evidence Standard for Future Docs
For each theory item, include:
1. `Theory`
2. `Branch`
3. `Code path`
4. `Automated test path`
5. `Failure mode covered`
6. `Open trade-off`

This keeps documentation honest, concise, and implementation-linked.

## Execution Update (2026-03-01)
Roadmap steps 1-4 are implemented in this branch with executable tests and CI verification.
