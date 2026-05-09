# Gateway

O `gateway` e a entrada unica da Alex Guedes Platform. Ele recebe as chamadas externas na porta `8080`, aplica rate limit por IP e encaminha cada rota para o servico correto.

## Responsabilidades

- Expor uma unica porta publica para a plataforma.
- Roteirizar `/auth/**` para o `auth-service`.
- Roteirizar `/sign/**` para o `signer-service`.
- Roteirizar `/validate/**` para o `validate-service`.
- Aplicar rate limit usando Redis.
- Expor metricas para Prometheus via Spring Actuator.

## Como funciona

1. O cliente chama `http://localhost:8080`.
2. O filtro global `RateLimitFilter` identifica o IP do cliente.
3. O gateway incrementa a chave `rate_limit:{ip}` no Redis.
4. Se a quantidade de chamadas ultrapassar a capacidade configurada, a resposta sera `429 Too Many Requests`.
5. Se a chamada estiver dentro do limite, o gateway encaminha para o servico responsavel pela rota.

## Rotas

| Rota no gateway | Destino padrao local | Destino no Docker |
| --- | --- | --- |
| `/auth/**` | `http://localhost:8082` | `http://auth-service:8082` |
| `/sign/**` | `http://localhost:8083` | `http://signer-service:8083` |
| `/validate/**` | `http://localhost:8081` | `http://validate-service:8081` |

## Configuracoes

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `REDIS_HOST` | `localhost` | Host do Redis usado pelo rate limit. |
| `REDIS_PORT` | `6379` | Porta do Redis. |
| `VALIDATE_SERVICE_URL` | `http://localhost:8081` | URL do validate-service. |
| `AUTH_SERVICE_URL` | `http://localhost:8082` | URL do auth-service. |
| `SIGNER_SERVICE_URL` | `http://localhost:8083` | URL do signer-service. |

Configuracao de rate limit em `application.yml`:

```yaml
app:
  rate-limit:
    capacity: 60
    window: 1m
```

## Executar localmente

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl gateway -am spring-boot:run
```

Para rodar com Docker, use o compose da pasta `infra`.

## Endpoints de observabilidade

- `GET /actuator/health`
- `GET /actuator/prometheus`

## Papel na arquitetura

Em uma plataforma parecida com producao, o gateway evita que cada microservico precise ser exposto diretamente. Ele centraliza preocupacoes transversais, como roteamento, limite de uso, headers e, futuramente, autenticacao, logs e correlacao de requisicoes.
