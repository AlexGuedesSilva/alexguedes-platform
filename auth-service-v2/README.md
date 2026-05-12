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

```text
com.alexguedes.platform.identity
|
+-- api
|   +-- controller
|   +-- dto
|   +-- exception
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
|
+-- shared
```

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
