# Stamp Coupling Example and Anti-Example

## Good (explicit contract)
Only include fields required by downstream consumers:
- `orderId`
- `correlationId`
- `eventType`
- minimal `payload`

This keeps producers free to evolve internal models.

## Anti-example (stamp coupling)
Sending an entire internal aggregate (`Order`, `Customer`, `Address`, `RiskProfile`, `AuditTrail`, etc.) as an event payload couples all consumers to producer internals.

Changes to unrelated internal fields can break consumers and force synchronized deployments.

See test: `saga-shared/src/test/kotlin/org/kurron/hard/parts/shared/StampCouplingTests.kt`.
