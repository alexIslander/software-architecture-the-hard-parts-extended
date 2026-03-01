# Runnable Walkthrough Commands

## Baseline verify
```bash
mvn clean verify
```

## Epic Saga (orchestrated)
```bash
docker compose up -d
mvn -pl orchestrator spring-boot:run
mvn -pl order-placement spring-boot:run
mvn -pl payment spring-boot:run
mvn -pl fulfillment spring-boot:run
mvn -pl email spring-boot:run
```

Example request:
```bash
curl -X POST http://localhost:8080/epic/purchase \
  -H 'Content-Type: application/json' \
  -d '{
    "customerEmail":"buyer@example.com",
    "itemSku":"BOOK-1",
    "amount":42.50,
    "idempotencyKey":"demo-epic-1"
  }'
```

## Anthology Saga (choreography)
```bash
docker compose up -d
mvn -pl order-placement spring-boot:run
mvn -pl payment spring-boot:run
mvn -pl fulfillment spring-boot:run
mvn -pl email spring-boot:run
```

Place order:
```bash
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerEmail":"buyer@example.com",
    "itemSku":"BOOK-1",
    "amount":42.50,
    "idempotencyKey":"demo-anthology-1"
  }'
```
