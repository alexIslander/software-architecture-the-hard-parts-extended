# Software Architecture The Hard Parts - Analysis Index

## Scope
This analysis correlates:
- This repository across all available branches (`main`, `epic-saga`, `phone-tag-saga`, `parallel-saga`, `anthology-saga`).
- Your note: [`What I learned from the book Software Architecture_ The Hard Parts.md`](./What%20I%20learned%20from%20the%20book%20Software%20Architecture_%20The%20Hard%20Parts.md)
- External diagram repo: <https://github.com/alexnsouza6/software-architecture-the-hard-parts>
- External mind-map repo: <https://github.com/isaacweathers/software-architecture-the-hard-parts-studyguide/tree/main/static/images>

Remote mirrors (`origin/*`, `upstream/*`) expose the same branch names above.

## Deliverables
1. [`01-theory-to-code-map.md`](./01-theory-to-code-map.md)
Theory matrix linking concepts to branch evidence and code coverage level.

2. [`02-branch-analysis.md`](./02-branch-analysis.md)
Branch-by-branch architecture intent vs implementation reality.

3. [`03-gaps-and-implementation-roadmap.md`](./03-gaps-and-implementation-roadmap.md)
Explicit missing implementations for important theories + KISS roadmap.

## Quick Take
- Strongest artifacts: `architectural-diagrams/workspace.dsl` + ADRs.
- Weakest artifacts: executable saga behavior in Kotlin code.
- `epic-saga` is the only branch with extra runtime modules (`orchestrator`, `specification-by-example`), but behavior is still mostly scaffold-level.
4. [`06-coverage-contract.md`](./06-coverage-contract.md)
Theory-to-implementation contract with code/test traceability.

5. [`07-stamp-coupling.md`](./07-stamp-coupling.md)
Explicit stamp-coupling example and anti-example.

6. [`08-runnable-walkthrough.md`](./08-runnable-walkthrough.md)
Runnable commands for implemented Epic/Anthology workflows.

7. [`10-branch-testing-guides.md`](./10-branch-testing-guides.md)
Per-branch testing guide template filled for implemented baseline flows and planned branches.

8. [`11-commit-tag-index.md`](./11-commit-tag-index.md)
Task-to-commit-to-tag traceability index for milestone tracking.
