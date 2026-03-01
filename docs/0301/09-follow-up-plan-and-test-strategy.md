# Follow-Up Plan and Test Strategy

Date: 2026-03-01
Owner: implementation follow-up
Status: Planned (no new implementation code yet)

## Confirmed Decisions
1. `main` remains a minimal baseline.
2. Every saga branch must contain its own full implementation.
3. Java/Spring migration (`Java 25`, `Spring Boot 4`) must be the last task.
4. Create clear, traceable commits and milestone tags.

## Target Branch Model
- `main`:
  - Minimal scaffold only.
  - Shared baseline docs, branch map, and contribution instructions.
- Saga branches (full, independent implementations):
  - `epic-saga`
  - `anthology-saga`
  - `phone-tag-saga`
  - `parallel-saga`
  - `fairy-tale-saga`
  - `time-travel-saga`
  - `fantasy-fiction-saga`
  - `horror-story-saga`

Each branch must include:
- runtime implementation code
- automated tests (happy + failure paths)
- branch-specific run/test docs
- known trade-offs and unsupported scenarios

## Execution Sequence

### Phase 0: Stabilize Baseline on `main`
- Keep only minimal baseline modules and neutral docs.
- Remove branch-specific implementation drift from `main`.
- Add branch navigation section in README.

Deliverable:
- `main` is clean and intentionally minimal.

### Phase 1: Branch-by-Branch Full Implementation
For each saga branch:
1. Implement complete workflow behavior for that saga type.
2. Add compensation/idempotency/retry/state handling where applicable.
3. Add contract checks and architecture fitness tests.
4. Add branch-local run/test walkthrough.

Deliverable per branch:
- full runnable implementation
- passing branch test suite
- branch docs with exact commands

### Phase 2: Documentation Completion and Traceability
- Update `docs/0301/01-theory-to-code-map.md` so statuses are factual per branch.
- Replace remaining stale `Missing` entries with:
  - `Implemented` (with code/test refs), or
  - `Planned` (with explicit scope/date if not yet done).
- Add theory-to-branch-to-test trace matrix.

Deliverable:
- docs reflect reality and are executable.

### Phase 3: Commit and Tag Hygiene
Commit policy:
- small, concern-scoped commits
- imperative commit messages
- branch-specific work only

Tag policy (local + push-ready):
- `v0-main-baseline`
- `v1-epic-saga-complete`
- `v2-anthology-saga-complete`
- `v3-sync-choreographed-complete`
- `v4-async-orchestrated-complete`
- `v5-all-saga-branches-complete`
- `v6-docs-traceability-complete`
- `v7-java25-boot4-migration-complete`

Deliverable:
- traceable history for rollback and study.

### Phase 4 (Last): Platform Migration
- Upgrade from current baseline to `Java 25` and `Spring Boot 4.x`.
- Perform migration after all branch functionality is complete.
- Resolve compatibility fallout branch-by-branch.

Deliverable:
- all branches migrated, rebuilt, and retested.

## Test Strategy (Per Branch)
Each branch must provide and pass:

1. Build gate
- `mvn clean verify`

2. Unit/component tests
- core service logic
- idempotency behavior
- retry/compensation decisions

3. Integration tests
- happy path end-to-end
- one failure per critical step
- recovery/resume where applicable

4. Contract tests
- schema conformance
- consumer-facing compatibility checks

5. Architecture fitness tests
- module boundary rules
- forbidden dependencies

6. Runtime smoke test
- infra startup command
- service startup command
- API/event scenario command
- expected observable result

## Required Testing Guide Template (to add per branch)
- Prerequisites
- Infra startup command
- App startup commands
- Happy-path test command
- Failure-path test command
- Resume/recovery command (if applicable)
- Teardown command
- Expected outputs

## Documentation Tasks to Execute
1. Update `docs/0301/01-theory-to-code-map.md`:
- Replace stale status rows.
- Add exact code/test paths for each implemented theory.

2. Update `docs/0301/04-Implementation-tasks.md`:
- Track by branch and completion state.

3. Add branch runbook references in docs index.

4. Add commit/tag index document:
- task -> commit hash -> tag mapping.

## Done Criteria for Follow-Up
- `main` remains minimal and clean.
- Each saga branch contains full implementation and tests.
- Docs status matrix is accurate and traceable.
- Clear commit history + milestone tags exist.
- Migration to Java 25 / Spring Boot 4 is completed last and validated.
