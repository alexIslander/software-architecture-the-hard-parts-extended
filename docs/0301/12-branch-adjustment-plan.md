# Branch Adjustment Plan

Date: 2026-03-01
Owner: repository maintainer

## Goal
Ensure all eight saga branches expose distinct, branch-local implementation evidence rather than sharing `main` unchanged.

## Scope
- Branches: `epic-saga`, `anthology-saga`, `phone-tag-saga`, `parallel-saga`, `fairy-tale-saga`, `time-travel-saga`, `fantasy-fiction-saga`, `horror-story-saga`.
- Evidence per branch:
  - branch-specific code/config marker
  - branch-specific runbook entry
  - branch-specific test command recorded and executed

## Execution Steps
1. Baseline check: capture current branch deltas against `main`.
2. Adjust one branch at a time with a dedicated branch marker commit.
3. Run targeted tests for that branch implementation style.
4. Update progress tracker with:
   - commit hash
   - delta count vs `main`
   - test command + pass/fail
5. Repeat until all eight branches have non-zero implementation deltas.

## Acceptance Criteria
- `git diff --name-only main..<branch> -- . ':(exclude)docs/**'` is non-empty for each branch.
- Each branch has a runbook/progress row updated with commit + test evidence.
- Progress file reflects current branch state and remaining work.
