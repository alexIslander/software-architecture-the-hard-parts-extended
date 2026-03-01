# Parallel Saga Branch Notes

This branch implements **Parallel Saga** (asynchronous + eventual + orchestrated).

## Implementation summary

- Orchestrator owns workflow state and exposes:
  - `purchase` to start workflow.
  - `advance` / `resume` to process async steps.
- After order creation, payment and fulfillment are attempted in the same parallel step.
- No compensation is triggered for branch failures.
- Eventual completion is achieved by repeated `resume`/`advance` calls until all required steps succeed.

## Difference from Fantasy Fiction

- `parallel-saga`: eventual outcome, no rollback compensation.
- `fantasy-fiction-saga`: atomic outcome, compensation required on downstream failures.
