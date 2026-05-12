# Gateway

The `gateway` module is the single public entry point for the Alex Guedes Platform. It receives external HTTP requests on port `8080`, applies Redis-backed rate limiting and forwards traffic to the correct internal service.

## Responsibilities

- Expose one public entry point for the platform.
- Route authentication traffic to Auth V1 or Auth V2.
- Route `/sign/**` to `signer-service`.
- Route `/validate/**` to `validate-service`.
- Apply rate limiting by client IP.
- Expose Actuator and Prometheus metrics.

## Routes

| Gateway route | Target | Default local URL |
| --- | --- | --- |
| `/auth/**` | Auth Service V1 legacy route | `http://localhost:8082` |
| `/auth/v1/**` | Auth Service V1 explicit route | `http://localhost:8082` |
| `/auth/v2/**` | Auth Service V2 route | `http://localhost:8085` |
| `/sign/**` | Signer Service | `http://localhost:8083` |
| `/validate/**` | Validate Service | `http://localhost:8081` |

`/auth/v1/**` and `/auth/v2/**` are rewritten before being forwarded, so `/auth/v2/login` becomes `/auth/login` inside `auth-service-v2`.

## Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `REDIS_HOST` | `localhost` | Redis host used by the rate limiter. |
| `REDIS_PORT` | `6379` | Redis port. |
| `VALIDATE_SERVICE_URL` | `http://localhost:8081` | Validate service URL. |
| `AUTH_SERVICE_V1_URL` | `http://localhost:8082` | Auth V1 URL. |
| `AUTH_SERVICE_V2_URL` | `http://localhost:8085` | Auth V2 URL. |
| `SIGNER_SERVICE_URL` | `http://localhost:8083` | Signer service URL. |

Rate limit configuration:

```yaml
app:
  rate-limit:
    capacity: 60
    window: 1m
```

## Run Locally

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl gateway -am spring-boot:run
```

## Observability

- `GET /actuator/health`
- `GET /actuator/prometheus`
