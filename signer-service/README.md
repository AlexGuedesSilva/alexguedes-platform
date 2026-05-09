# Signer Service

O `signer-service` gera assinaturas HMAC SHA-256 para requisicoes. Ele existe para ajudar no aprendizado do fluxo de assinatura: o cliente informa os dados da requisicao e recebe `timestamp`, `nonce` e `signature`.

## Responsabilidades

- Receber os dados que representam uma requisicao.
- Gerar `timestamp` quando ele nao e informado.
- Gerar `nonce` quando ele nao e informado.
- Montar o payload canonico da assinatura.
- Calcular HMAC SHA-256 em Base64.
- Retornar a assinatura e os metadados necessarios para validacao.
- Expor metricas para Prometheus via Spring Actuator.

## Como funciona

1. O cliente chama `POST /sign`.
2. O servico usa o `timestamp` enviado ou cria um com o horario atual.
3. O servico usa o `nonce` enviado ou cria um UUID.
4. O corpo da requisicao e transformado em hash SHA-256.
5. O payload canonico e montado neste formato:

```text
METHOD
PATH
BODY_SHA256_HEX
TIMESTAMP
NONCE
```

6. O payload e assinado com HMAC SHA-256 usando o segredo do cliente.
7. A resposta retorna a assinatura em Base64.

## Endpoint

```http
POST /sign
Content-Type: application/json
```

Exemplo:

```json
{
  "key": "demo-key",
  "secret": "demo-secret",
  "method": "POST",
  "path": "/orders",
  "body": "{\"amount\":100}"
}
```

Resposta:

```json
{
  "success": true,
  "message": "signature generated",
  "data": {
    "key": "demo-key",
    "method": "POST",
    "path": "/orders",
    "bodyHash": "hash-do-body",
    "timestamp": 1714490000,
    "nonce": "uuid-gerado",
    "signature": "assinatura-base64"
  },
  "timestamp": "2026-05-04T12:00:00Z"
}
```

## Configuracoes

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `HMAC_CLIENT_SECRET` | `demo-secret` | Segredo usado quando o request nao envia `secret`. |

## Executar localmente

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl signer-service -am spring-boot:run
```

Porta padrao: `8083`.

## Endpoints de observabilidade

- `GET /actuator/health`
- `GET /actuator/prometheus`

## Papel na arquitetura

Em uma producao real, normalmente o cliente assinaria suas proprias requisicoes sem chamar um servico interno para isso. Aqui, o `signer-service` funciona como ferramenta didatica para visualizar exatamente como a assinatura HMAC precisa ser gerada antes de chamar o `validate-service`.
