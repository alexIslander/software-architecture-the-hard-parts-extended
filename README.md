# Phone Tag Saga Branch Notes

This branch implements **Phone Tag Saga** (synchronous + atomic + choreographed).

## Implementation summary

- Workflow is owned by `order-placement` (no dedicated orchestrator service).
- Steps are synchronous:
  - charge payment
  - reserve fulfillment
  - send email
- Atomic behavior is enforced with compensation:
  - fulfillment failure triggers payment refund
  - email failure triggers fulfillment cancel and payment refund
- Failed workflows can be retried with `resume`.

## Difference from Epic Saga

- `epic-saga` centralizes coordination in `orchestrator`.
- `phone-tag-saga` keeps coordination inside the initiating order service, increasing service coupling.
