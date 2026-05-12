# Auth Service V2

Auth Service V2 is the next-generation identity and authentication platform for the AlexGuedes Platform ecosystem.

This service is responsible for:

- User identity management
- Authentication
- Authorization
- JWT issuance
- Refresh token lifecycle
- API Key management
- Distributed identity validation
- Cryptographic trust between services

---

# Architecture

The service follows a modular and domain-oriented architecture.

```text
auth-service-v2
|
├── authentication
├── authorization
├── identity
├── session
├── token
├── apikey
├── audit
├── shared
└── infrastructure
```

---

# Core Concepts

## Identity

Represents platform users and credentials.

## Authentication

Responsible for validating credentials and issuing tokens.

## Authorization

Responsible for RBAC, scopes and permissions.

## Session Management

Responsible for refresh tokens and active sessions.

## Token Service

Responsible for JWT issuance, validation and revocation.

---

# Security

The platform is designed to support:

- RS256 JWT signing
- JWKS public key exposure
- Token rotation
- Secure refresh token lifecycle
- Distributed token validation
- API Key hashing
- RBAC authorization

---

# Tech Stack

- Java 21
- Spring Boot
- Spring Security
- PostgreSQL
- Redis
- Flyway
- JWT
- Docker
- Maven

---

# Database Migration

Flyway is used for database versioning.

```text
src/main/resources/db/migration
```

Example:

```text
V1__create_identity_tables.sql
V2__create_roles.sql
V3__create_permissions.sql
V4__create_refresh_tokens.sql
```

---

# Initial Features

- User registration
- Login
- JWT authentication
- Refresh tokens
- Token validation
- Session revocation

---

# Future Roadmap

- RS256 asymmetric signing
- JWKS endpoint
- RBAC permissions
- OAuth2 Authorization Server
- Multi-device sessions
- Audit trails
- API Key management
- Token introspection
- Service-to-service authentication

---

# Platform Vision

Auth Service V2 is part of a distributed identity platform architecture designed for scalability, cryptographic trust, and microservice ecosystems.