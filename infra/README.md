# Infra

The `infra` folder contains the local Docker Compose environment for the Alex Guedes Platform. It orchestrates Redis, PostgreSQL, the gateway, application services and Prometheus.

## Files

- `docker-compose.yml`: starts the local platform.
- `postgres/init-databases.sql`: creates the `authdb_v1` and `authdb_v2` databases on first PostgreSQL initialization.
- `redis/redis.conf`: Redis configuration with AOF enabled.
- `prometheus/prometheus.yml`: metrics scraping configuration.

## Docker Compose Services

| Service | Port | Description |
| --- | --- | --- |
| `gateway` | `8080` | Single entry point for the platform. |
| `validate-service` | `8081` | HMAC validation and replay attack protection. |
| `auth-service` | `8082` | Legacy authentication service. |
| `signer-service` | `8083` | HMAC signature generation. |
| `auth-service-v2` | `8085` | Distributed identity platform foundation. |
| `redis` | `6379` | Stores rate limit counters and nonces. |
| `postgres` | `5432` | Stores identity/authentication data. |
| `prometheus` | `9090` | Scrapes service metrics. |

## Start

```powershell
cd C:\projetos2026\alexguedes-platform\infra
docker compose up --build
```

## Stop

```powershell
cd C:\projetos2026\alexguedes-platform\infra
docker compose down
```

## PostgreSQL Databases

On first startup, PostgreSQL creates:

- `authdb_v1`
- `authdb_v2`

If the `postgres_data` volume already exists, Docker will not rerun the initialization script. Recreate the volume only when you intentionally want a clean local database.

## Redis Keys

- `rate_limit:{ip}`: temporary request counter per client IP.
- `nonce:{key}:{nonce}`: reserved nonce used to block replay attacks.

## Observability

Prometheus scrapes:

- `gateway:8080`
- `validate-service:8081`
- `auth-service:8082`
- `auth-service-v2:8085`
- `signer-service:8083`

## Role In The Architecture

This folder simulates a local runtime environment for a production-like microservice platform. In a real production environment, the same responsibilities would usually be handled by Kubernetes, ECS, Docker Swarm or another orchestration platform, with managed secrets and a dedicated observability stack.
