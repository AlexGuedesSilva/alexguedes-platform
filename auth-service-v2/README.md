# Auth Service V2

`auth-service-v2` is the next identity service for the Alex Guedes Platform. Its goal is to evolve the project from a simple authentication service into a distributed identity platform with authentication, authorization, session management, API key ownership and auditability.

## Current Status

The service currently has:

- Spring Boot application bootstrap.
- PostgreSQL and Flyway configuration.
- H2-backed test configuration for local builds.
- Domain model for identity, credentials, roles, permissions, sessions, refresh tokens, API keys and audit events.
- Database migrations aligned with the domain model.
- Actuator and Prometheus metrics support.
- Gateway and Docker Compose integration.

The next step is to implement repositories, application services and HTTP endpoints.

## Package Direction

The service follows a package-by-capability direction. Instead of organizing the
whole codebase only by technical layers, each major authentication concern gets
its own package. This makes the project easier to study because each folder
answers one business question: login, permissions, users, sessions, tokens,
API keys, audit or infrastructure.

```text
auth-service-v2
|
+-- src/main/java/com/alexguedes/platform/identity
    |
    +-- authentication
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- authorization
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- identity
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- session
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- token
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- apikey
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- audit
    |   +-- api
    |   +-- application
    |   +-- domain
    |   +-- infrastructure
    |
    +-- shared
    |   +-- config
    |   +-- constants
    |   +-- exception
    |   +-- response
    |
    +-- infrastructure
        +-- observability
        +-- persistence
        +-- redis
        +-- security
```

### Package Responsibilities

| Package | Responsibility |
| --- | --- |
| `authentication` | Login, credential validation, password checks and authentication flows. |
| `authorization` | Roles, permissions, access rules and authorization decisions. |
| `identity` | User profile, account status, registration and identity lifecycle. |
| `session` | User sessions, session revocation and active session tracking. |
| `token` | Access tokens, refresh tokens, token rotation, JWT/JWKS evolution. |
| `apikey` | API key creation, ownership, hashing, listing and revocation. |
| `audit` | Security-sensitive event records such as login, logout, token refresh and API key actions. |
| `shared` | Reusable service-level helpers such as responses, exceptions, constants and common configuration. |
| `infrastructure` | Cross-cutting technical adapters such as database, Redis, security wiring, metrics and external integrations. |

### Internal Package Pattern

Feature packages may use the same internal structure when the feature becomes
large enough:

```text
feature
|
+-- api
|   +-- controller
|   +-- dto
|
+-- application
|   +-- service
|   +-- usecase
|   +-- exception
|
+-- domain
|   +-- model
|   +-- policy
|
+-- infrastructure
|   +-- repository
|   +-- security
|   +-- persistence
```

- `api`: HTTP controllers and request/response DTOs.
- `application`: use cases and orchestration of domain rules.
- `domain`: business model, value objects, policies and domain rules.
- `infrastructure`: database repositories, external clients, framework adapters and persistence details.

Small packages do not need all four folders immediately. They should grow only
when there is real code to place there.

## Domain Model

The initial domain model is centered around:

- `IdentityUser`: platform identity profile.
- `UserCredential`: password credential stored as a hash.
- `Role`: RBAC role such as `ADMIN` or `USER`.
- `Permission`: fine-grained permission such as `api-key:create`.
- `UserRole`: role assignment for a user.
- `RolePermission`: permission assignment for a role.
- `UserSession`: logical user session and revocation target.
- `RefreshToken`: hashed refresh token with expiration, rotation and revocation fields.
- `ApiKey`: API key owned by a user and stored only as a hash.
- `AuditEvent`: immutable security-sensitive event record.

## Database Migrations

Flyway migrations are located at:

```text
src/main/resources/db/migration
```

Current migrations:

- `V1__create_identity_tables.sql`
- `V2__create_roles.sql`
- `V3__create_permissions.sql`
- `V4__create_refresh_tokens.sql`
- `V5__create_api_keys.sql`
- `V6__create_audit_events.sql`

## Planned Endpoints

The gateway exposes V2 through `/auth/v2/**` and rewrites that path to `/auth/**` inside this service.

Planned service endpoints:

| Method | Path | Responsibility |
| --- | --- | --- |
| `POST` | `/auth/register` | Create identity and credentials. |
| `POST` | `/auth/login` | Validate credentials and create a session. |
| `POST` | `/auth/refresh` | Rotate refresh token and issue a new access token. |
| `POST` | `/auth/logout` | Revoke the current session or refresh token. |
| `GET` | `/auth/me` | Return the authenticated identity, roles and permissions. |
| `POST` | `/auth/api-keys` | Create an API key owned by the current user. |
| `GET` | `/auth/api-keys` | List API keys owned by the current user. |
| `DELETE` | `/auth/api-keys/{id}` | Revoke an owned API key. |
| `GET` | `/actuator/health` | Health check. |
| `GET` | `/actuator/prometheus` | Prometheus metrics. |

## Security Roadmap

- JWT authentication filter.
- Stateless `SecurityFilterChain`.
- `/auth/me` endpoint.
- Roles and permissions in JWT claims.
- Method and route authorization.
- API key ownership enforcement.
- Complete refresh token rotation.
- Session revocation.
- Audit event writing for all sensitive actions.
- RS256 JWT signing.
- JWKS endpoint for distributed validation.

## Build

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl auth-service-v2 -am test
```

## Runtime

Default local port:

```text
8085
```

Default local database:

```text
jdbc:postgresql://localhost:5432/authdb_v2
```

Docker Compose creates `authdb_v2` during PostgreSQL initialization.
