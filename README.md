# FinFlow — Distributed Fintech Payment Platform

A distributed payment platform built with microservices architecture.

## Services
- **Wallet Service** (port 8081) — wallet management, top-up, transfers, Redis caching
- **Transaction Service** (port 8082) — transaction ledger, Kafka consumer, RabbitMQ producer
- **Notification Service** (port 8083) — RabbitMQ consumer, notification delivery
- **Fraud Detection Service** (port 8084) — Kafka consumer, rule-based fraud detection

## Tech Stack
- Java 17, Spring Boot 3.4.1
- PostgreSQL (per-service DB isolation)
- Apache Kafka (event streaming)
- RabbitMQ (task queue)
- Redis (caching + idempotency)
- Docker Compose

## Architecture
