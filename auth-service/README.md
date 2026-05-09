# Auth Service

O `auth-service` e o servico responsavel por registrar usuarios, autenticar credenciais, gerar JWT e emitir API keys persistidas com armazenamento seguro.

## Responsabilidades

- Registrar usuarios em banco PostgreSQL.
- Promover automaticamente o primeiro usuario cadastrado para `ADMIN`.
- Receber credenciais de login.
- Validar usuario e senha com hash BCrypt.
- Gerar access token JWT assinado.
- Gerar refresh tokens persistidos apenas como hash e com suporte a revogacao.
- Gerar API keys com prefixo `ak_`, persistindo apenas o hash da chave.
- Expor metricas para Prometheus via Spring Actuator.

## Como funciona

1. O cliente cria o primeiro usuario em `POST /auth/register`.
2. O `AuthService` normaliza `username` e `email`, valida duplicidade e salva a senha com BCrypt.
3. Se ainda nao existem usuarios, o cadastro recebe o papel `ADMIN`; os proximos recebem `USER`.
4. O cliente envia `username` e `password` para `POST /auth/login`.
5. Se as credenciais estiverem corretas, o `JwtService` gera um access token JWT com `subject`, `userId`, `role`, `issuer`, emissao, expiracao e assinatura.
6. O `RefreshTokenService` cria um refresh token, salva apenas seu hash SHA-256 e retorna o valor original uma unica vez.
7. Para renovar sessao, o cliente chama `POST /auth/refresh`; o refresh token antigo e revogado e um novo e emitido.
8. Para encerrar sessao, o cliente chama `POST /auth/logout`; o refresh token informado e revogado.
9. Para gerar uma API key, o cliente chama `POST /auth/api-key` com o access token JWT no header `Authorization`.

## Endpoints

### Registrar usuario

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "username": "alex",
  "email": "alex@example.com",
  "password": "strong-password"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "user registered",
  "data": {
    "id": "uuid-do-usuario",
    "username": "alex",
    "email": "alex@example.com",
    "role": "ADMIN",
    "createdAt": "2026-05-08T21:00:00Z"
  },
  "timestamp": "2026-05-08T21:00:00Z"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "alex",
  "password": "strong-password"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "authenticated",
  "data": {
    "accessToken": "jwt-gerado",
    "refreshToken": "rt_refresh-token-gerado",
    "tokenType": "Bearer",
    "expiresInSeconds": 3600
  },
  "timestamp": "2026-05-04T12:00:00Z"
}
```

### Renovar token

```http
POST /auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "rt_refresh-token-gerado"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "token refreshed",
  "data": {
    "accessToken": "novo-jwt",
    "refreshToken": "novo-rt",
    "tokenType": "Bearer",
    "expiresInSeconds": 3600
  },
  "timestamp": "2026-05-08T21:00:00Z"
}
```

### Logout

```http
POST /auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "rt_refresh-token-gerado"
}
```

### Gerar API key

```http
POST /auth/api-key
Authorization: Bearer jwt-gerado
Content-Type: application/json
```

```json
{
  "name": "portfolio-client"
}
```

Resposta:

```json
{
  "success": true,
  "message": "api key generated",
  "data": {
    "id": "uuid-da-api-key",
    "key": "ak_chave-gerada",
    "prefix": "ak_prefixo",
    "name": "portfolio-client",
    "createdAt": "2026-05-08T21:00:00Z"
  },
  "timestamp": "2026-05-04T12:00:00Z"
}
```

Importante: o valor de `key` aparece apenas nessa resposta. No banco fica salvo somente o hash SHA-256 em `api_keys.key_hash`.

## Configuracoes

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `JWT_SECRET` | `change-me-to-a-very-long-secret-key-32-bytes` | Segredo usado para assinar o JWT. |
| `JWT_EXPIRES_IN_SECONDS` | `3600` | Tempo de expiracao do JWT. |
| `REFRESH_TOKEN_EXPIRES_IN_SECONDS` | `604800` | Tempo de expiracao do refresh token. |

## Executar localmente

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl auth-service -am spring-boot:run
```

Porta padrao: `8082`.

## Endpoints de observabilidade

- `GET /actuator/health`
- `GET /actuator/prometheus`

## Papel na arquitetura

Este servico representa a camada de identidade da plataforma. O projeto ja persiste usuarios, armazena senhas com BCrypt, persiste API keys apenas como hash e possui refresh tokens com rotacao e revogacao. Em uma producao real, os proximos passos seriam politicas de senha, auditoria, validacao de JWT no gateway, endpoint para listar/revogar API keys e testes automatizados.
