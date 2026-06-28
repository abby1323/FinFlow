# FinFlow — Distributed Fintech Payment Platform

A distributed payment platform built with microservices architecture, demonstrating event-driven design, async messaging, and production-grade patterns like idempotency, caching, and fault isolation.

---

## Architecture

```
                          ┌─────────────────────────────────────────────────────┐
                          │                    Client                           │
                          └──────────────────────┬──────────────────────────────┘
                                                 │ REST
                                                 ▼
                          ┌─────────────────────────────────────────────────────┐
                          │              Wallet Service :8081                   │
                          │                                                     │
                          │  • Wallet creation & balance management             │
                          │  • Top-up & peer-to-peer transfers                  │
                          │  • Redis: balance cache (TTL 30s) + idempotency     │
                          │  • PostgreSQL: wallets, topups                      │
                          └──────────┬──────────────────────────────────────────┘
                                     │ Publishes: TransactionInitiatedEvent
                                     │ Topic: transaction.initiated
                                     ▼
                          ┌─────────────────────────────────────────────────────┐
                          │                  Apache Kafka                       │
                          │           topic: transaction.initiated              │
                          │                  3 partitions                       │
                          └──────────┬──────────────────────┬───────────────────┘
                                     │                      │
                     consumer-group: │                      │ consumer-group:
                     transaction-    │                      │ fraud-service-group
                     service-group   │                      │
                                     ▼                      ▼
          ┌──────────────────────────────┐    ┌─────────────────────────────────┐
          │   Transaction Service :8082  │    │   Fraud Detection Service :8084  │
          │                              │    │                                  │
          │  • Consumes Kafka events     │    │  • Consumes same Kafka events    │
          │  • Persists transaction      │    │  • Runs rule engine:             │
          │    ledger to PostgreSQL      │    │    - High Amount (> ₹50,000)     │
          │  • Exposes transaction       │    │    - High Frequency              │
          │    history REST API          │    │  • Persists fraud alerts         │
          │  • PostgreSQL: transactions  │    │  • PostgreSQL: fraud_alerts      │
          └──────────┬───────────────────┘    └─────────────────────────────────┘
                     │ Publishes: NotificationEvent
                     │ Exchange: finflow.exchange
                     │ Queue: finflow.notification.queue
                     ▼
          ┌──────────────────────────────┐
          │          RabbitMQ            │
          │   finflow.notification.queue │
          └──────────┬───────────────────┘
                     │
                     ▼
          ┌──────────────────────────────┐
          │  Notification Service :8083  │
          │                              │
          │  • Consumes RabbitMQ queue   │
          │  • Simulates email/SMS send  │
          │  • Logs delivery to DB       │
          │  • PostgreSQL: notification_ │
          │    logs                      │
          └──────────────────────────────┘
```

---

## Technology Decisions

| Technology | Used For | Why Not Something Else |
|---|---|---|
| **Kafka** | TransactionInitiated events | Fan-out to multiple consumers (Transaction + Fraud). Replay capability. Ordered per wallet via partition key. |
| **RabbitMQ** | Notification delivery | Task queue — exactly one consumer should send each notification. No fan-out needed. |
| **Redis** | Balance caching + idempotency keys | Sub-millisecond reads for hot balance data. Atomic `SET NX` for duplicate request detection. |
| **PostgreSQL per service** | Data persistence | True DB isolation — each service owns its schema. No shared state between services. |
| **Flyway** | Schema migrations | Versioned, auditable schema changes. `ddl-auto: none` in all services. |

---

## Services

### Wallet Service (port 8081)
Owns wallet lifecycle and money movement. The only service that modifies balances.

**Key design decisions:**
- Idempotency keys stored in Redis (TTL 24h) prevent duplicate top-ups and transfers
- Balance cached in Redis with 30s TTL — invalidated on every write
- Kafka message key = `walletId` — guarantees ordering of events per wallet across partitions
- DB constraint on `idempotency_key` as a safety net if Redis is unavailable

**Endpoints:**
```
POST   /api/v1/wallets/{userId}          — create wallet
GET    /api/v1/wallets/{userId}/balance  — get balance (Redis-cached)
POST   /api/v1/wallets/topup             — top up wallet
POST   /api/v1/wallets/transfer          — transfer between wallets
```

---

### Transaction Service (port 8082)
The transaction ledger. Consumes Kafka events and persists authoritative transaction records.

**Key design decisions:**
- `existsById` check before insert — idempotent consumer (safe on Kafka retry)
- Native SQL `INSERT` used instead of JPA `save()` to avoid Hibernate merge issues with externally assigned UUIDs
- Publishes to RabbitMQ after successful DB write — notification is decoupled from transaction processing

**Endpoints:**
```
GET    /api/v1/transactions/{transactionId}       — get transaction by ID
GET    /api/v1/transactions/wallet/{walletId}     — get wallet transaction history
```

---

### Notification Service (port 8083)
Consumes from RabbitMQ and handles notification delivery. Decoupled from transaction processing — if it goes down, messages queue up and are processed on recovery.

**Key design decisions:**
- RabbitMQ chosen over Kafka here because notifications are tasks (process once), not events (replay/fan-out)
- Durable queue — messages survive RabbitMQ restarts
- Delivery logged to DB for audit trail

**Endpoints:**
```
GET    /api/v1/notifications/wallet/{walletId}           — notifications for wallet
GET    /api/v1/notifications/transaction/{transactionId} — notifications for transaction
```

---

### Fraud Detection Service (port 8084)
Independent Kafka consumer that runs fraud rules asynchronously. Uses a different consumer group from Transaction Service — both receive every event independently.

**Key design decisions:**
- Separate consumer group (`fraud-service-group`) means Fraud Service gets its own copy of every event — independent of Transaction Service
- Rule engine is pluggable — each rule implements `FraudRule` interface
- Fraud analysis is async and never blocks the payment flow

**Current rules:**
- `HIGH_AMOUNT` — flags transactions exceeding ₹50,000 (configurable)
- `HIGH_FREQUENCY` — flags wallets exceeding 5 transactions per minute (configurable)

**Endpoints:**
```
GET    /api/v1/fraud/alerts                    — all open fraud alerts
GET    /api/v1/fraud/alerts/wallet/{walletId}  — alerts for specific wallet
```

---

## Running Locally

### Prerequisites
- Java 17
- Docker + Docker Compose
- Maven

### Start infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL, Redis, Kafka (KRaft mode — no ZooKeeper), and RabbitMQ.

### Create databases

```bash
docker exec -it finflow-postgres psql -U postgres -c "CREATE DATABASE wallet_db;"
docker exec -it finflow-postgres psql -U postgres -c "CREATE DATABASE transaction_db;"
docker exec -it finflow-postgres psql -U postgres -c "CREATE DATABASE notification_db;"
docker exec -it finflow-postgres psql -U postgres -c "CREATE DATABASE fraud_db;"
```

### Start services

Start each service from its directory:

```bash
# Terminal 1
cd wallet-service && mvn spring-boot:run

# Terminal 2
cd transaction-service && mvn spring-boot:run

# Terminal 3
cd notification-service && mvn spring-boot:run

# Terminal 4
cd fraud-service && mvn spring-boot:run
```

### RabbitMQ Management UI
Available at `http://localhost:15672` (guest/guest)

---

## End-to-End Flow Example

```bash
# 1. Create a wallet
curl -X POST http://localhost:8081/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11

# 2. Top up ₹1,000
curl -X POST http://localhost:8081/api/v1/wallets/topup \
  -H "Content-Type: application/json" \
  -d '{"userId":"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","amount":1000.00,"idempotencyKey":"topup-001"}'

# 3. Check balance (served from Redis cache)
curl http://localhost:8081/api/v1/wallets/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/balance

# 4. Check transaction history
curl http://localhost:8082/api/v1/transactions/wallet/<walletId>

# 5. Trigger fraud detection (amount > ₹50,000)
curl -X POST http://localhost:8081/api/v1/wallets/topup \
  -H "Content-Type: application/json" \
  -d '{"userId":"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","amount":75000.00,"idempotencyKey":"topup-large"}'

# 6. Check fraud alerts
curl http://localhost:8084/api/v1/fraud/alerts
```

---

## Project Structure

```
FinFlow/
├── docker-compose.yml
├── README.md
├── wallet-service/          — Spring Boot 3.4.1, port 8081
├── transaction-service/     — Spring Boot 3.4.1, port 8082
├── notification-service/    — Spring Boot 3.4.1, port 8083
└── fraud-service/           — Spring Boot 3.4.1, port 8084
```