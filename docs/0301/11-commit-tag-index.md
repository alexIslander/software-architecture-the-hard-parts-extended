# Commit and Tag Index

Date: 2026-03-01
Scope: follow-up traceability before Java 25 / Spring Boot 4 migration.

## Milestone Mapping
| Task | Expected Commit(s) | Expected Tag | Current State |
|---|---|---|---|
| Stabilize `main` baseline and keep Java 21 | TBD after commit | `v0-main-baseline` | In progress |
| Complete `epic-saga` branch implementation and tests | TBD after commit | `v1-epic-saga-complete` | Planned |
| Complete `anthology-saga` branch implementation and tests | TBD after commit | `v2-anthology-saga-complete` | Planned |
| Complete synchronous choreographed branch (`phone-tag`) | TBD after commit | `v3-sync-choreographed-complete` | Planned |
| Complete asynchronous orchestrated branch (`parallel`) | TBD after commit | `v4-async-orchestrated-complete` | Planned |
| Finish all eight saga branches | TBD after commit | `v5-all-saga-branches-complete` | Planned |
| Finalize docs and traceability matrix | TBD after commit | `v6-docs-traceability-complete` | In progress |
| Migrate to Java 25 and Spring Boot 4 | TBD after commit | `v7-java25-boot4-migration-complete` | Not started |

## Usage
1. Fill commit hashes only after tests are green for the scope.
2. Create tags only after corresponding branch docs and runbooks are updated.
3. Keep Java 25 migration tag strictly last.
