# Infra

A pasta `infra` contem os recursos para subir a plataforma localmente com Docker Compose. Ela orquestra Redis, Gateway, servicos da aplicacao e Prometheus.

## Componentes

- `docker-compose.yml`: sobe todos os containers da plataforma.
- `redis/redis.conf`: configuracao simples do Redis com AOF habilitado.
- `prometheus/prometheus.yml`: configuracao de coleta de metricas dos servicos.

## Servicos do Docker Compose

| Servico | Porta | Descricao |
| --- | --- | --- |
| `gateway` | `8080` | Entrada unica da plataforma. |
| `validate-service` | `8081` | Validacao HMAC e replay attack. |
| `auth-service` | `8082` | Login, JWT e API key. |
| `signer-service` | `8083` | Geracao de assinatura HMAC. |
| `redis` | `6379` | Armazena rate limit e nonces. |
| `prometheus` | `9090` | Coleta metricas dos servicos. |

## Subir a plataforma

```powershell
cd C:\projetos2026\alexguedes-platform\infra
docker compose up --build
```

## Parar a plataforma

```powershell
cd C:\projetos2026\alexguedes-platform\infra
docker compose down
```

## Fluxo de infraestrutura

1. O Redis sobe primeiro e fica disponivel para gateway e validate-service.
2. O `validate-service` usa Redis para reservar nonces.
3. O `gateway` usa Redis para contar requisicoes por IP.
4. Os servicos Spring Boot sobem expondo `/actuator/prometheus`.
5. O Prometheus coleta metricas de `gateway`, `auth-service`, `signer-service` e `validate-service`.

## Chaves usadas no Redis

- `rate_limit:{ip}`: contador temporario de requisicoes por IP.
- `nonce:{key}:{nonce}`: nonce reservado para impedir replay attack.

## Papel na arquitetura

Esta pasta simula o ambiente de execucao local da plataforma. Em uma producao real, os mesmos conceitos seriam distribuidos em Kubernetes, ECS, Docker Swarm ou outra plataforma de orquestracao, com segredos gerenciados por ferramenta propria e metricas integradas a uma stack de observabilidade.
