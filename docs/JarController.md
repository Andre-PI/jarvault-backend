# Documentação do JarController

Este documento descreve como usar os endpoints do `JarController` (`/api/jar`). Inclui descrições, exemplos de requisição (curl) e formatos de resposta.

Base: arquivo `src/main/java/com/avorio/jar_vault/controller/JarController.java`

Endpoints:

1. POST /api/jar/upload
- Descrição: Faz o upload de um arquivo JAR para o servidor.
- Método: POST
- Content-Type: multipart/form-data
- Parâmetro: `file` (campo do formulário) — arquivo jar
- Resposta: 200 OK com corpo text/plain contendo "Jar uploaded successfully"

Exemplo curl:

```bash
curl -X POST "http://localhost:8080/api/jar/upload" \
  -F "file=@/caminho/para/seu/mod.jar" \
  -H "Accept: text/plain"
```

Notas:
- O servidor espera um arquivo; o campo do formulário deve ser `file`.
- Se houver validação/erro, a API pode retornar erros tratados pela camada global de exceções (ver `GlobalExceptionHandler`).

2. POST /api/jar/upload/batch
- Descrição: Faz o upload de múltiplos arquivos JAR de uma vez.
- Método: POST
- Content-Type: multipart/form-data
- Parâmetro: `files` (campo do formulário, múltiplos arquivos) — array de arquivos jar
- Resposta: 200 OK com corpo JSON contendo `Message` com estatísticas do upload

Exemplo curl:

```bash
curl -X POST "http://localhost:8080/api/jar/upload/batch" \
  -F "files=@/caminho/para/mod1.jar" \
  -F "files=@/caminho/para/mod2.jar" \
  -F "files=@/caminho/para/mod3.jar" \
  -H "Accept: application/json"
```

Exemplo de resposta:

```json
{
  "message": "Batch upload completed: 3 succeeded, 0 failed"
}
```

Notas:
- O servidor processa cada arquivo individualmente e registra o resultado.
- Mesmo que alguns arquivos falhem, o endpoint retorna 200 OK com estatísticas.
- O campo do formulário deve ser `files` (plural).
- Cada arquivo é validado e processado independentemente.

3. GET /api/jar/get/all
- Descrição: Recupera uma página de JARs armazenados.
- Método: GET
- Parâmetros de paginação (Spring `Pageable` via query params):
  - `page` — número da página (0-based)
  - `size` — tamanho da página (padrão 20)
  - `sort` — campo e direção (ex: `name,asc`)
- Resposta: 200 OK com corpo JSON representando `Page<JarDTO>`.

Exemplo curl:

```bash
curl "http://localhost:8080/api/jar/get/all?page=0&size=20&sort=name,asc" -H "Accept: application/json"
```

Exemplo de resposta (fragmento):

```json
{
  "content": [
    { "id": 1, "name": "example.jar", "version": "1.0.0", "size": 102400 },
    { "id": 2, "name": "other.jar", "version": "2.0.1", "size": 204800 }
  ],
  "pageable": { /* ... */ },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": { /* ... */ },
  "first": true,
  "numberOfElements": 2
}
```

4. POST /api/jar/delete
- Descrição: Deleta em batch uma lista de JARs identificados por seus IDs.
- Método: POST
- Content-Type: application/json
- Corpo (JSON): objeto `BatchDeleteRequest` com os campos:
  - `listOfIds`: array de números (IDs) — obrigatória
  - `passwordKey`: string — senha/-chave necessária para autorizar a operação (conforme configuração da aplicação)

Exemplo de corpo JSON:

```json
{
  "listOfIds": [1, 2, 3],
  "passwordKey": "SUA_CHAVE_AQUI"
}
```

Exemplo curl:

```bash
curl -X POST "http://localhost:8080/api/jar/delete" \
  -H "Content-Type: application/json" \
  -d '{"listOfIds":[1,2,3], "passwordKey":"SUA_CHAVE"}'
```

Resposta: 200 OK com JSON:

```json
{ "message": "Jars deleted successfully" }
```

Erros comuns e tratamento:
- 400 Bad Request: parâmetros inválidos (ex: corpo vazio, ids nulos)
- 401/403: autorização inválida (se a API exigir autenticação)
- 404 Not Found: recursos não encontrados (quando aplicável)
- 500 Internal Server Error: erro do servidor (ver logs)

Notas de implementação:
- `JarDTO` campos: `id` (Long), `name` (String), `version` (String), `size` (Long).
- `BatchDeleteRequest` campos: `listOfIds` (Long[]), `passwordKey` (String).
- Upload usa `MultipartFile` no controller; certifique-se de que o cliente envie o campo `file` corretamente.

Sugestões de uso:
- Para downloads, verifique se há algum endpoint público/privado para recuperar o arquivo pelo ID (não documentado aqui). Se não existir, considere adicionar um endpoint GET /api/jar/{id}/download.
- Proteja endpoints sensíveis (delete/upload) com autenticação/autorização.

---

Arquivo gerado automaticamente em `docs/JarController.md`.
