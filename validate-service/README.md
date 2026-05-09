# Validate Service

O `validate-service` verifica assinaturas HMAC e impede replay attack. Ele e o centro do fluxo de seguranca da plataforma.

## Responsabilidades

- Validar se a chave do cliente e conhecida.
- Validar se o `timestamp` esta dentro da janela permitida.
- Exigir `nonce` para cada chamada assinada.
- Recalcular a assinatura HMAC e comparar com a assinatura enviada.
- Comparar assinaturas em tempo constante.
- Registrar o nonce no Redis para impedir reutilizacao.
- Expor metricas para Prometheus via Spring Actuator.

## Como funciona

1. O cliente chama `POST /validate` com `key`, `method`, `path`, `body`, `timestamp`, `nonce` e `signature`.
2. O servico confere se `key` e igual ao cliente configurado.
3. O servico verifica se o `timestamp` esta dentro da janela `allowed-skew`.
4. O servico exige que `nonce` e `signature` estejam presentes.
5. O `HmacValidator` recria o mesmo payload canonico usado pelo `signer-service`.
6. O hash HMAC esperado e calculado com o segredo configurado.
7. A assinatura esperada e comparada com a assinatura recebida usando comparacao em tempo constante.
8. O `RedisReplayAttackGuard` tenta gravar `nonce:{key}:{nonce}` no Redis usando `SET NX` com TTL.
9. Se o nonce ja existir, a chamada e rejeitada como replay attack.
10. Se tudo passar, a resposta indica `signature accepted`.

## Endpoint

```http
POST /validate
Content-Type: application/json
```

Exemplo:

```json
{
  "key": "demo-key",
  "method": "POST",
  "path": "/orders",
  "body": "{\"amount\":100}",
  "timestamp": 1714490000,
  "nonce": "uuid-gerado",
  "signature": "assinatura-base64"
}
```

Resposta de sucesso:

```json
{
  "success": true,
  "message": "signature accepted",
  "data": {
    "valid": true,
    "message": "signature accepted"
  },
  "timestamp": "2026-05-04T12:00:00Z"
}
```

Possiveis mensagens de erro:

- `unknown client key`
- `timestamp outside allowed window`
- `nonce is required`
- `signature is required`
- `invalid signature`
- `replay attack detected`

## Configuracoes

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `REDIS_HOST` | `localhost` | Host do Redis. |
| `REDIS_PORT` | `6379` | Porta do Redis. |
| `HMAC_CLIENT_KEY` | `demo-key` | Chave aceita pelo validador. |
| `HMAC_CLIENT_SECRET` | `demo-secret` | Segredo usado para recalcular a assinatura. |
| `HMAC_ALLOWED_SKEW` | `PT5M` | Janela de tolerancia do timestamp. |
| `HMAC_NONCE_TTL` | `PT10M` | Tempo de vida do nonce no Redis. |

## Executar localmente

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl validate-service -am spring-boot:run
```

Porta padrao: `8081`.

Este servico precisa de Redis para validar replay attack.

## Endpoints de observabilidade

- `GET /actuator/health`
- `GET /actuator/prometheus`

## Papel na arquitetura

Este modulo simula uma camada de protecao de APIs usada em integracoes entre sistemas. Mesmo que alguem capture uma requisicao assinada, a combinacao de `timestamp` curto e `nonce` armazenado no Redis impede que a mesma assinatura seja reaproveitada indefinidamente.
