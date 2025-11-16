# 📦 Documentação da API Modrinth - JarVault

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Endpoints Disponíveis](#endpoints-disponíveis)
3. [Status da API](#1-verificar-status-da-api)
4. [Buscar Projetos](#2-buscar-projetos-mods-shaders-modpacks)
5. [Detalhes de Projeto](#3-obter-detalhes-de-um-projeto)
6. [Versões de Projeto](#4-obter-versões-de-um-projeto)
7. [Detalhes de Versão](#5-obter-detalhes-de-uma-versão)
8. [Estrutura dos DTOs](#estrutura-dos-dtos)
9. [Exemplos Práticos](#exemplos-práticos-de-uso)
10. [Filtros e Facets](#guia-completo-de-filtros-facets)

---

## Visão Geral

A integração com a API do Modrinth permite buscar, filtrar e obter informações detalhadas sobre mods, shaders, modpacks e resource packs para Minecraft.

**Base URL:** `http://localhost:8080/api/modrinth`

**Formato de Resposta:** JSON

---

## Endpoints Disponíveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/status` | Verifica se a API do Modrinth está online |
| GET | `/search` | Busca projetos com filtros avançados |
| GET | `/project/{projectId}` | Detalhes de um projeto específico |
| GET | `/project/{projectId}/versions` | Lista versões de um projeto |
| GET | `/version/{versionId}` | Detalhes de uma versão específica |

---

## 1. Verificar Status da API

### Endpoint
```
GET /api/modrinth/status
```

### Descrição
Verifica se a API do Modrinth está acessível e funcionando.

### Parâmetros
Nenhum

### Resposta de Sucesso
```json
{
  "message": "Modrinth API is online"
}
```

### Resposta de Erro
```json
{
  "message": "Modrinth API is down"
}
```

### Exemplo
```bash
curl http://localhost:8080/api/modrinth/status
```

---

## 2. Buscar Projetos (Mods, Shaders, Modpacks)

### Endpoint
```
GET /api/modrinth/search
```

### Descrição
Busca projetos no Modrinth com suporte a filtros avançados, ordenação e paginação.

### Parâmetros de Query

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|-----------|------|-------------|---------|-----------|
| `query` | String | Não | - | Texto de busca livre |
| `facets` | String[] | Não | - | Filtros de busca (pode repetir) |
| `index` | String | Não | `relevance` | Critério de ordenação |
| `offset` | Integer | Não | `0` | Número de resultados a pular |
| `limit` | Integer | Não | `20` | Número máximo de resultados |

### Valores Válidos para `index`
- `relevance` - Mais relevantes (padrão)
- `downloads` - Mais baixados
- `follows` - Mais seguidos
- `newest` - Mais recentes
- `updated` - Atualizados recentemente

### Resposta
```json
{
  "hits": [
    {
      "slug": "sodium",
      "title": "Sodium",
      "description": "Modern rendering engine and client-side optimization mod",
      "categories": ["fabric", "optimization"],
      "clientSide": "required",
      "serverSide": "unsupported",
      "projectType": "mod",
      "downloads": 88043233,
      "iconUrl": "https://cdn.modrinth.com/data/...",
      "projectId": "AANobbMI",
      "author": "jellysquid3",
      "versions": ["1.20.1", "1.19.4", "..."],
      "latestVersion": "mc1.20.1-0.5.3"
    }
  ],
  "offset": 0,
  "limit": 20,
  "totalHits": 1
}
```

### Exemplos

#### Busca Simples
```bash
curl "http://localhost:8080/api/modrinth/search?query=sodium&limit=5"
```

#### Buscar Apenas Mods
```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:mod&limit=10"
```

#### Buscar Shaders Populares
```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:shader&index=downloads&limit=10"
```

#### Buscar Mods de Otimização para Fabric
```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:mod&facets=categories:fabric&facets=categories:optimization&index=downloads"
```

#### Buscar Modpacks para Minecraft 1.20.1
```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:modpack&facets=versions:1.20.1&limit=10"
```

#### Buscar Mods Client-Side Only
```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:mod&facets=client_side:required&facets=server_side:unsupported"
```

---

## 3. Obter Detalhes de um Projeto

### Endpoint
```
GET /api/modrinth/project/{projectId}
```

### Descrição
Obtém informações detalhadas sobre um projeto específico. O `projectId` pode ser o **slug** (nome) ou o **ID** do projeto.

### Parâmetros de Path

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `projectId` | String | Slug ou ID do projeto (ex: "sodium" ou "AANobbMI") |

### Resposta
```json
{
  "slug": "sodium",
  "title": "Sodium",
  "description": "Modern rendering engine and client-side optimization mod for Minecraft",
  "categories": ["fabric", "optimization", "neoforge"],
  "clientSide": "required",
  "serverSide": "unsupported",
  "projectType": "mod",
  "downloads": 88043233,
  "iconUrl": "https://cdn.modrinth.com/data/AANobbMI/icon.png",
  "projectId": "AANobbMI",
  "author": "jellysquid3",
  "versions": ["1.20.1", "1.19.4", "..."],
  "latestVersion": "mc1.20.1-0.5.3"
}
```

### Campos Importantes

- **clientSide**: `required`, `optional` ou `unsupported`
  - `required` - Deve estar instalado no cliente
  - `optional` - Pode ou não estar no cliente
  - `unsupported` - Não funciona no cliente

- **serverSide**: `required`, `optional` ou `unsupported`
  - `required` - Deve estar instalado no servidor
  - `optional` - Pode ou não estar no servidor
  - `unsupported` - Não funciona no servidor

- **projectType**: `mod`, `shader`, `modpack` ou `resourcepack`

### Exemplos

#### Por Slug
```bash
curl http://localhost:8080/api/modrinth/project/sodium
```

#### Por ID
```bash
curl http://localhost:8080/api/modrinth/project/AANobbMI
```

---

## 4. Obter Versões de um Projeto

### Endpoint
```
GET /api/modrinth/project/{projectId}/versions
```

### Descrição
Lista todas as versões disponíveis de um projeto, com opção de filtrar por loader e versão do Minecraft.

### Parâmetros de Path

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `projectId` | String | Slug ou ID do projeto |

### Parâmetros de Query

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `loaders` | String[] | Não | Filtrar por loader (pode repetir) |
| `gameVersions` | String[] | Não | Filtrar por versão do Minecraft (pode repetir) |

### Loaders Disponíveis
- `fabric`
- `forge`
- `quilt`
- `neoforge`

### Resposta
```json
[
  {
    "id": "rAfhHfow",
    "projectId": "AANobbMI",
    "name": "Sodium 0.5.3",
    "versionNumber": "mc1.20.1-0.5.3",
    "changelog": "### Changes\n- Fixed rendering issues\n- Improved performance",
    "dependencies": [
      {
        "versionId": null,
        "projectId": "P7dR8mSH",
        "dependencyType": "required",
        "fileName": "fabric-api"
      }
    ],
    "gameVersions": ["1.20.1"],
    "versionType": "release",
    "loaders": ["fabric"],
    "featured": true,
    "downloads": 1000000,
    "datePublished": "2023-08-01T10:00:00Z",
    "files": [
      {
        "hashes": {
          "sha512": "abc123...",
          "sha1": "def456..."
        },
        "url": "https://cdn.modrinth.com/data/AANobbMI/versions/rAfhHfow/sodium-fabric-mc1.20.1-0.5.3.jar",
        "filename": "sodium-fabric-mc1.20.1-0.5.3.jar",
        "primary": true,
        "size": 524288
      }
    ]
  }
]
```

### Campos Importantes

#### Dependencies
- **dependencyType**: Tipo de dependência
  - `required` - Obrigatório para funcionar
  - `optional` - Adiciona funcionalidades extras
  - `incompatible` - Não pode ser usado junto
  - `embedded` - Já está incluído no arquivo

#### Version Type
- **versionType**: Tipo da versão
  - `release` - Versão estável
  - `beta` - Versão beta
  - `alpha` - Versão alpha

#### Files
- **url**: Link direto para download do arquivo
- **filename**: Nome do arquivo
- **primary**: Se é o arquivo principal (geralmente true)
- **size**: Tamanho em bytes
- **hashes**: SHA1 e SHA512 para verificação de integridade

### Exemplos

#### Todas as Versões
```bash
curl http://localhost:8080/api/modrinth/project/sodium/versions
```

#### Versões para Fabric
```bash
curl "http://localhost:8080/api/modrinth/project/sodium/versions?loaders=fabric"
```

#### Versões para Minecraft 1.20.1
```bash
curl "http://localhost:8080/api/modrinth/project/sodium/versions?gameVersions=1.20.1"
```

#### Versões para Fabric e Minecraft 1.20.1
```bash
curl "http://localhost:8080/api/modrinth/project/sodium/versions?loaders=fabric&gameVersions=1.20.1"
```

#### Múltiplas Versões do Minecraft
```bash
curl "http://localhost:8080/api/modrinth/project/sodium/versions?gameVersions=1.20.1&gameVersions=1.19.4"
```

---

## 5. Obter Detalhes de uma Versão

### Endpoint
```
GET /api/modrinth/version/{versionId}
```

### Descrição
Obtém informações detalhadas de uma versão específica, incluindo links de download.

### Parâmetros de Path

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `versionId` | String | ID da versão |

### Resposta
```json
{
  "id": "rAfhHfow",
  "projectId": "AANobbMI",
  "name": "Sodium 0.5.3",
  "versionNumber": "mc1.20.1-0.5.3",
  "changelog": "### Changes\n- Fixed rendering issues\n- Improved performance",
  "dependencies": [
    {
      "versionId": null,
      "projectId": "P7dR8mSH",
      "dependencyType": "required",
      "fileName": "fabric-api"
    }
  ],
  "gameVersions": ["1.20.1"],
  "versionType": "release",
  "loaders": ["fabric"],
  "featured": true,
  "downloads": 1000000,
  "datePublished": "2023-08-01T10:00:00Z",
  "files": [
    {
      "hashes": {
        "sha512": "abc123...",
        "sha1": "def456..."
      },
      "url": "https://cdn.modrinth.com/data/AANobbMI/versions/rAfhHfow/sodium-fabric-mc1.20.1-0.5.3.jar",
      "filename": "sodium-fabric-mc1.20.1-0.5.3.jar",
      "primary": true,
      "size": 524288
    }
  ]
}
```

### Exemplo
```bash
curl http://localhost:8080/api/modrinth/version/rAfhHfow
```

---

## Estrutura dos DTOs

### ModrinthProjectDTO

```typescript
{
  slug: string;              // Nome amigável do projeto
  title: string;             // Título do projeto
  description: string;       // Descrição detalhada
  categories: string[];      // Categorias do projeto
  clientSide: string;        // "required" | "optional" | "unsupported"
  serverSide: string;        // "required" | "optional" | "unsupported"
  projectType: string;       // "mod" | "shader" | "modpack" | "resourcepack"
  downloads: number;         // Total de downloads
  iconUrl: string;           // URL do ícone
  projectId: string;         // ID único do projeto
  author: string;            // Nome do autor
  versions: string[];        // Lista de versões disponíveis
  latestVersion: string;     // Versão mais recente
}
```

### ModrinthVersionDTO

```typescript
{
  id: string;                // ID único da versão
  projectId: string;         // ID do projeto
  name: string;              // Nome da versão
  versionNumber: string;     // Número da versão
  changelog: string;         // Notas de atualização
  dependencies: [            // Dependências
    {
      versionId: string;
      projectId: string;
      dependencyType: string; // "required" | "optional" | "incompatible" | "embedded"
      fileName: string;
    }
  ];
  gameVersions: string[];    // Versões compatíveis do Minecraft
  versionType: string;       // "release" | "beta" | "alpha"
  loaders: string[];         // Loaders compatíveis
  featured: boolean;         // Se é versão destacada
  downloads: number;         // Downloads desta versão
  datePublished: string;     // Data de publicação
  files: [                   // Arquivos para download
    {
      hashes: {
        sha512: string;
        sha1: string;
      };
      url: string;           // Link direto para download
      filename: string;      // Nome do arquivo
      primary: boolean;      // Se é arquivo principal
      size: number;          // Tamanho em bytes
    }
  ];
}
```

### ModrinthSearchResponse

```typescript
{
  hits: ModrinthProjectDTO[]; // Lista de projetos encontrados
  offset: number;             // Offset da busca
  limit: number;              // Limite de resultados
  totalHits: number;          // Total de resultados encontrados
}
```

---

## Guia Completo de Filtros (Facets)

Os filtros (facets) permitem refinar sua busca. Cada facet é passado como um parâmetro `facets` separado na query string.

### Sintaxe
```
facets=tipo:valor
```

### Tipos de Projeto

| Facet | Descrição |
|-------|-----------|
| `project_type:mod` | Mods |
| `project_type:shader` | Shaders |
| `project_type:modpack` | Modpacks |
| `project_type:resourcepack` | Resource Packs |

### Loaders

| Facet | Descrição |
|-------|-----------|
| `categories:fabric` | Projetos para Fabric |
| `categories:forge` | Projetos para Forge |
| `categories:quilt` | Projetos para Quilt |
| `categories:neoforge` | Projetos para NeoForge |

### Categorias de Mods

| Facet | Descrição |
|-------|-----------|
| `categories:optimization` | Otimização |
| `categories:adventure` | Aventura |
| `categories:decoration` | Decoração |
| `categories:magic` | Magia |
| `categories:technology` | Tecnologia |
| `categories:utility` | Utilidades |
| `categories:world-generation` | Geração de Mundo |
| `categories:cursed` | Cursed (mods estranhos) |
| `categories:food` | Comida |
| `categories:equipment` | Equipamentos |
| `categories:storage` | Armazenamento |

### Versões do Minecraft

| Facet | Descrição |
|-------|-----------|
| `versions:1.21.1` | Minecraft 1.21.1 |
| `versions:1.20.1` | Minecraft 1.20.1 |
| `versions:1.19.4` | Minecraft 1.19.4 |
| `versions:1.18.2` | Minecraft 1.18.2 |

### Client/Server Side

| Facet | Descrição |
|-------|-----------|
| `client_side:required` | Obrigatório no cliente |
| `client_side:optional` | Opcional no cliente |
| `client_side:unsupported` | Não funciona no cliente |
| `server_side:required` | Obrigatório no servidor |
| `server_side:optional` | Opcional no servidor |
| `server_side:unsupported` | Não funciona no servidor |

---

## Exemplos Práticos de Uso

### Caso 1: Encontrar e Baixar um Mod

**Passo 1:** Buscar o mod
```bash
curl "http://localhost:8080/api/modrinth/search?query=sodium&facets=project_type:mod" | jq
```

**Passo 2:** Pegar o `projectId` do resultado (ex: "AANobbMI")

**Passo 3:** Buscar versões compatíveis
```bash
curl "http://localhost:8080/api/modrinth/project/AANobbMI/versions?loaders=fabric&gameVersions=1.20.1" | jq
```

**Passo 4:** Pegar o `url` do campo `files[0].url` e baixar
```bash
wget "https://cdn.modrinth.com/data/AANobbMI/versions/.../sodium.jar"
```

### Caso 2: Listar Shaders Populares

```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:shader&index=downloads&limit=20" | jq '.hits[] | {title, downloads, slug}'
```

### Caso 3: Mods de Otimização Client-Side Only

```bash
curl "http://localhost:8080/api/modrinth/search?facets=project_type:mod&facets=categories:optimization&facets=client_side:required&facets=server_side:unsupported&index=downloads" | jq
```

### Caso 4: Verificar Dependências de um Mod

```bash
curl "http://localhost:8080/api/modrinth/project/sodium/versions?loaders=fabric&gameVersions=1.20.1" | jq '.[0].dependencies'
```

### Caso 5: Buscar Modpacks de Aventura

```bash
curl "http://localhost:8080/api/modrinth/search?query=adventure&facets=project_type:modpack&facets=versions:1.20.1&index=follows&limit=10" | jq
```

### Caso 6: Verificar Compatibilidade Cliente/Servidor

```bash
curl "http://localhost:8080/api/modrinth/project/sodium" | jq '{title, clientSide, serverSide}'
```

**Resultado:**
```json
{
  "title": "Sodium",
  "clientSide": "required",
  "serverSide": "unsupported"
}
```
Isso significa: Sodium deve estar no cliente, mas não funciona no servidor.

---

## Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| 200 | Sucesso |
| 400 | Requisição inválida |
| 404 | Projeto/Versão não encontrado |
| 500 | Erro interno do servidor |

---

## Notas Importantes

1. **Rate Limiting**: Respeite os limites de requisições da API do Modrinth
2. **Cache**: Considere fazer cache das respostas para reduzir chamadas
3. **IDs vs Slugs**: Prefira usar IDs quando possível, pois slugs podem mudar
4. **Paginação**: Use `offset` e `limit` para navegar por resultados grandes
5. **Filtros Múltiplos**: Você pode combinar múltiplos facets para buscas precisas
6. **Verificação de Hash**: Sempre verifique os hashes SHA1/SHA512 dos arquivos baixados

---

## Suporte e Referências

- **API Original do Modrinth**: https://docs.modrinth.com/api
- **Repositório do Projeto**: /home/avorio/jar_vault
- **Código Fonte**: `src/main/java/com/avorio/jar_vault/service/ModrinthServiceImpl.java`

---

**Última Atualização:** Novembro 2025  
**Versão da Documentação:** 1.0

