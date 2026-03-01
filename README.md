# Anthology Saga Branch Notes

This branch implements **Anthology Saga** (asynchronous + eventual + choreographed).

## Implementation summary

- Workflow is choreographed through event handlers in each service.
- `order-placement` maintains the projection/read-model of saga progress.
- Out-of-order events are buffered and reconciled once dependencies are satisfied.
- Duplicate events are ignored using processed event tracking.
- Failure events update projection state without centralized orchestration.

## Branch-specific additions

- Anthology projection API in `order-placement`:
  - `GET /anthology/orders/{orderId}/projection`
  - `POST /anthology/reconcile`
- Dedicated tests for projection behavior under:
  - out-of-order events,
  - duplicate failure events,
  - delayed completion events.
