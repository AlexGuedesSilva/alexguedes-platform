# Shared

O `shared` e uma biblioteca interna Maven usada pelos servicos da plataforma. Ele concentra DTOs e utilitarios comuns para evitar duplicacao entre `auth-service`, `signer-service` e `validate-service`.

## Responsabilidades

- Padronizar respostas HTTP com `ApiResponse`.
- Definir o contrato de entrada para assinatura e validacao com `SignatureRequest`.
- Definir o contrato de saida da assinatura com `SignatureResponse`.
- Fornecer utilitarios de hash, HMAC e payload canonico.

## Componentes principais

### `ApiResponse`

Formato padrao de resposta:

```json
{
  "success": true,
  "message": "mensagem",
  "data": {},
  "timestamp": "2026-05-04T12:00:00Z"
}
```

### `SignatureRequest`

Contrato usado por `/sign` e `/validate`:

```json
{
  "key": "demo-key",
  "secret": "demo-secret",
  "method": "POST",
  "path": "/orders",
  "body": "{\"amount\":100}",
  "timestamp": 1714490000,
  "nonce": "uuid",
  "signature": "assinatura-base64"
}
```

### `SignatureResponse`

Contrato retornado pelo `signer-service`:

```json
{
  "key": "demo-key",
  "method": "POST",
  "path": "/orders",
  "bodyHash": "sha256-hex",
  "timestamp": 1714490000,
  "nonce": "uuid",
  "signature": "assinatura-base64"
}
```

### `HashUtils`

Funcoes principais:

- `hmacSha256Base64`: calcula HMAC SHA-256 e retorna Base64.
- `sha256Hex`: calcula SHA-256 e retorna hexadecimal.
- `constantTimeEquals`: compara assinaturas reduzindo risco de timing attack.
- `canonicalSignaturePayload`: monta o payload usado na assinatura.

Payload canonico:

```text
METHOD
PATH
BODY_SHA256_HEX
TIMESTAMP
NONCE
```

## Executar build do modulo

```powershell
cd C:\projetos2026\alexguedes-platform
mvn -pl shared clean package
```

## Papel na arquitetura

O `shared` representa uma dependencia comum entre microservicos. Em projetos reais, esse tipo de modulo deve ser pequeno e estavel, porque qualquer mudanca nele pode impactar varios servicos ao mesmo tempo.
