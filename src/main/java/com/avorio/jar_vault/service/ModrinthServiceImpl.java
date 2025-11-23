package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.GameVersionDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.modrinth.*;
import com.avorio.jar_vault.exception.ApiOfflineException;
import com.avorio.jar_vault.exception.RateLimitException;
import com.avorio.jar_vault.exception.ResourceNotFoundException;
import com.avorio.jar_vault.utils.ModrinthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModrinthServiceImpl implements ModrinthService {

    private final ModrinthUtil modrinthUtil;
    @Value("${api.modrinth.url}")
    private String modrinthApiUrl;

    private final RestTemplate restTemplate;

    public ModrinthServiceImpl(RestTemplate restTemplate, ModrinthUtil modrinthUtil) {
        this.restTemplate = restTemplate;
        this.modrinthUtil = modrinthUtil;
    }

    @Override
    public Message modrinthApiStatus() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(modrinthApiUrl, String.class);
            return response.getStatusCode().is2xxSuccessful()
                    ? new Message("Modrinth API is online")
                    : new Message("Modrinth API returned status: " + response.getStatusCode());
        } catch (Exception e) {
            throw new ApiOfflineException("Modrinth API is down: " + e.getMessage());
        }
    }

    @Override
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public ModrinthSearchResponse searchProjects(String query, List<String> facets, String index, Integer offset, Integer limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(modrinthApiUrl + "/v2/search");

            if (query != null && !query.isEmpty()) {
                builder.queryParam("query", query);
            }

            if (facets != null && !facets.isEmpty()) {
                String facetsJson = "[" + facets.stream()
                        .map(facet -> "[\"" + facet + "\"]")
                        .collect(Collectors.joining(",")) + "]";
                builder.queryParam("facets", facetsJson);
            }

            if (index != null && !index.isEmpty()) {
                builder.queryParam("index", index);
            }

            if (offset != null) {
                builder.queryParam("offset", offset);
            }

            if (limit != null) {
                builder.queryParam("limit", limit);
            }

            URI uri = builder.encode().build().toUri();
            ResponseEntity<ModrinthSearchResponse> response = restTemplate.getForEntity(
                    uri,
                    ModrinthSearchResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to search projects on Modrinth");
            }

            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API error: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to search projects on Modrinth: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "modrinthProjects", key = "#projectId")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public ModrinthProjectDTO getProject(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID não pode ser vazio");
        }

        try {
            String url = modrinthApiUrl + "/v2/project/" + projectId;
            ResponseEntity<ModrinthProjectDTO> response = restTemplate.getForEntity(url, ModrinthProjectDTO.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to get project from Modrinth");
            }
            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Projeto " + projectId + " não encontrado na Modrinth");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get project from Modrinth: " + e.getMessage());
        }
    }

    public List<ModrinthProjectInfoDTO> getProjectInfo(String projectId, String loaders, String gameVersions) {
        StringBuilder urlBuilder = new StringBuilder(modrinthApiUrl + "/v2/project/" + projectId + "/version");
        boolean hasParams = false;

        if(loaders != null && !loaders.isEmpty()) {
            urlBuilder.append("?loaders=").append(modrinthUtil.buildModrinthParamPattern(loaders));
            hasParams = true;
            System.out.println("Loaders param: " + modrinthUtil.buildModrinthParamPattern(loaders));
        }
        if(gameVersions != null && !gameVersions.isEmpty()) {
            urlBuilder.append(hasParams ? "&" : "?").append("game_versions=").append(modrinthUtil.buildModrinthParamPattern(gameVersions));
            System.out.println("Game versions param: " + modrinthUtil.buildModrinthParamPattern(gameVersions));
        }

        String url = urlBuilder.toString();
        System.out.println("Final URL: " + url);

        ResponseEntity<List<ModrinthProjectInfoDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    @Override
    public List<ModrinthProjectDTO> getProjectsInBulk(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        // A Modrinth aceita IDs no formato: ["id1", "id2"]
        String idsJson = "[" + projectIds.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(",")) + "]";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(modrinthApiUrl + "/v2/projects")
                .queryParam("ids", idsJson);

        try {
            ResponseEntity<List<ModrinthProjectDTO>> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            // Log o erro aqui
            return List.of();
        }
    }
    @Override
    @Cacheable(value = "modrinthVersions", key = "#projectId + '_' + #loaders + '_' + #gameVersions")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )

    public List<ModrinthVersionDTO> getProjectVersions(String projectId, List<String> loaders, List<String> gameVersions) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID não pode ser vazio");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                    modrinthApiUrl + "/v2/project/" + projectId + "/version"
            );

            if (loaders != null && !loaders.isEmpty()) {
                String loadersJson = "[\"" + String.join("\",\"", loaders) + "\"]";
                builder.queryParam("loaders", loadersJson);
            }

            if (gameVersions != null && !gameVersions.isEmpty()) {
                String gameVersionsJson = "[\"" + String.join("\",\"", gameVersions) + "\"]";
                builder.queryParam("game_versions", gameVersionsJson);
            }

            URI uri = builder.encode().build().toUri();
            ResponseEntity<List<ModrinthVersionDTO>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to get project versions from Modrinth");
            }

            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Versões do projeto " + projectId + " não encontradas na Modrinth");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get project versions from Modrinth: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "modrinthVersionDetails", key = "#versionId")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public ModrinthVersionDTO getVersion(String versionId) {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID não pode ser vazio");
        }

        try {
            String url = modrinthApiUrl + "/v2/version/" + versionId;
            ResponseEntity<ModrinthVersionDTO> response = restTemplate.getForEntity(url, ModrinthVersionDTO.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to get version from Modrinth");
            }

            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Versão " + versionId + " não encontrada na Modrinth");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API is offline or inaccessible: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get version from Modrinth: " + e.getMessage());
        }
    }

    @Override
    public List<ModrinthVersionDTO.DependencyDTO> getVersionDependencies(String versionId) {
        ModrinthVersionDTO version = getVersion(versionId);
        return version.getDependencies() != null ? version.getDependencies() : List.of();
    }

    @Override
    public List<ModrinthVersionDTO.DependencyDTO> getVersionDependenciesByType(String versionId, String dependencyType) {
        List<ModrinthVersionDTO.DependencyDTO> dependencies = getVersionDependencies(versionId);
        return dependencies.stream()
                .filter(dep -> dep.getDependencyType().equalsIgnoreCase(dependencyType))
                .collect(Collectors.toList());
    }

    @Override
    public EnrichedVersionDTO getEnrichedVersion(String versionId) {
        ModrinthVersionDTO version = getVersion(versionId);
        EnrichedVersionDTO enrichedVersion = new EnrichedVersionDTO();

        // Copiar campos básicos
        enrichedVersion.setId(version.getId());
        enrichedVersion.setProjectId(version.getProjectId());
        enrichedVersion.setName(version.getName());
        enrichedVersion.setVersionNumber(version.getVersionNumber());
        enrichedVersion.setChangelog(version.getChangelog());
        enrichedVersion.setGameVersions(version.getGameVersions());
        enrichedVersion.setVersionType(version.getVersionType());
        enrichedVersion.setLoaders(version.getLoaders());
        enrichedVersion.setFeatured(version.getFeatured());
        enrichedVersion.setDownloads(version.getDownloads());
        enrichedVersion.setDatePublished(version.getDatePublished());
        enrichedVersion.setFiles(version.getFiles());

        // Enriquecer dependências
        enrichedVersion.setDependencies(getEnrichedDependencies(versionId));

        return enrichedVersion;
    }

    @Override
    public List<EnrichedDependencyDTO> getEnrichedDependencies(String versionId) {
        List<ModrinthVersionDTO.DependencyDTO> dependencies = getVersionDependencies(versionId);

        if (dependencies.isEmpty()) {
            return List.of();
        }

        // Coletar todos os projectIds únicos para fazer uma única chamada bulk
        List<String> projectIds = dependencies.stream()
                .map(ModrinthVersionDTO.DependencyDTO::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (projectIds.isEmpty()) {
            return dependencies.stream()
                    .map(dep -> {
                        EnrichedDependencyDTO enriched = new EnrichedDependencyDTO();
                        enriched.setProjectId(dep.getProjectId());
                        enriched.setVersionId(dep.getVersionId());
                        enriched.setDependencyType(dep.getDependencyType());
                        return enriched;
                    })
                    .collect(Collectors.toList());
        }

        // Buscar todos os projetos de uma vez (evita N+1)
        Map<String, ModrinthProjectDTO> projectsMap = getProjectsBulk(projectIds);

        return dependencies.stream()
                .map(dep -> {
                    EnrichedDependencyDTO enriched = new EnrichedDependencyDTO();
                    enriched.setProjectId(dep.getProjectId());
                    enriched.setVersionId(dep.getVersionId());
                    enriched.setDependencyType(dep.getDependencyType());

                    ModrinthProjectDTO project = projectsMap.get(dep.getProjectId());
                    if (project != null) {
                        enriched.setProjectName(project.getTitle());
                        enriched.setProjectSlug(project.getSlug());
                        enriched.setIconUrl(project.getIconUrl());
                        enriched.setDescription(project.getDescription());
                    }
                    return enriched;
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca múltiplos projetos de uma vez usando a API bulk da Modrinth.
     * Evita o problema de N+1 queries.
     */
    @Cacheable(value = "modrinthProjectsBulk", key = "#projectIds.hashCode()")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public Map<String, ModrinthProjectDTO> getProjectsBulk(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        try {
            // A API da Modrinth aceita até 100 IDs por vez
            // Se tivermos mais, precisamos dividir em lotes
            final int BATCH_SIZE = 100;
            Map<String, ModrinthProjectDTO> allProjects = new java.util.HashMap<>();

            for (int i = 0; i < projectIds.size(); i += BATCH_SIZE) {
                List<String> batch = projectIds.subList(i, Math.min(i + BATCH_SIZE, projectIds.size()));

                String idsParam = "[\"" + String.join("\",\"", batch) + "\"]";
                String url = modrinthApiUrl + "/v2/projects?ids=" + idsParam;

                ResponseEntity<List<ModrinthProjectDTO>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    response.getBody().forEach(project ->
                        allProjects.put(project.getProjectId(), project)
                    );
                }
            }

            return allProjects;
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get projects in bulk from Modrinth: " + e.getMessage());
        }
    }

    @Override
    public ModrinthProjectDTO getEnrichedProject(String projectId) {
        ModrinthProjectDTO project = getProject(projectId);

        try {
            List<ModrinthVersionDTO> versions = getProjectVersions(projectId, null, null);

            if (versions != null && !versions.isEmpty()) {
                ModrinthVersionDTO latestVersion = versions.get(0);

                List<EnrichedDependencyDTO> enrichedDeps = getEnrichedDependencies(latestVersion.getId());
                project.setDependencies(enrichedDeps);
            } else {
                project.setDependencies(List.of());
            }
        } catch (Exception e) {
            project.setDependencies(List.of());
        }

        return project;
    }

    @Override
    @Cacheable(value = "modrinthCategories")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public List<Categories> getCategories() {
        try {
            String url = modrinthApiUrl + "/v2/tag/category";
            ResponseEntity<List<Categories>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to get categories from Modrinth");
            }

            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get categories from Modrinth: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "modrinthGameVersions")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public List<GameVersionDTO> getMInecraftVersions() {
        try {
            String url = modrinthApiUrl + "/v2/tag/game_version";
            ResponseEntity<List<GameVersionDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().stream()
                        .filter(gameVersionDTO -> "release".equals(gameVersionDTO.getVersionType()))
                        .toList();
            }

            throw new ApiOfflineException("Failed to get game versions from Modrinth");
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get game versions from Modrinth: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "modrinthProjectDependencies", key = "#projectId")
    @Retryable(
            retryFor = {RestClientException.class, ApiOfflineException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public ModrinthProjectsResponse getDependenciesFromProject(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID não pode ser vazio");
        }

        try {
            String url = modrinthApiUrl + "/v2/project/" + projectId + "/dependencies";
            ResponseEntity<ModrinthProjectsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiOfflineException("Failed to get dependencies from Modrinth");
            }

            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Rate limit excedido na Modrinth API. Aguarde antes de tentar novamente.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Dependências do projeto " + projectId + " não encontradas na Modrinth");
        } catch (ResourceAccessException e) {
            throw new ApiOfflineException("Modrinth API está offline ou inacessível: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiOfflineException("Failed to get dependencies from Modrinth: " + e.getMessage());
        }
    }

    @Override
    public List<ModrinthVersionDTO> getClientDependenciesFromProject(String projectId, String loader, String gameVersion) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID não pode ser vazio");
        }
        if (loader == null || loader.trim().isEmpty()) {
            throw new IllegalArgumentException("Loader não pode ser vazio");
        }
        if (gameVersion == null || gameVersion.trim().isEmpty()) {
            throw new IllegalArgumentException("Game version não pode ser vazia");
        }

        List<ModrinthVersionDTO> versions = getProjectVersions(projectId, List.of(loader), List.of(gameVersion));

        if (versions == null || versions.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma versão encontrada para o projeto " + projectId);
        }

        versions.stream()
                .filter(ver -> ver.getLoaders().contains(loader) && ver.getGameVersions().contains(gameVersion))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma versão compatível encontrada para loader '" + loader +
                        "' e game version '" + gameVersion + "'"
                ));

        return versions;
    }

    @Override
    public List<EnrichedDependencyDTO> getTransitiveDependencies(String versionId, String dependencyType, int maxDepth) {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID não pode ser vazio");
        }
        if (maxDepth < 1) {
            maxDepth = 1;
        }
        if (maxDepth > 10) {
            maxDepth = 10;
        }

        Set<String> processedVersions = new HashSet<>();
        Map<String, EnrichedDependencyDTO> allDependencies = new LinkedHashMap<>();

        collectTransitiveDependencies(versionId, dependencyType, maxDepth, 0, processedVersions, allDependencies);

        return new ArrayList<>(allDependencies.values());
    }

    private void collectTransitiveDependencies(
            String versionId,
            String dependencyType,
            int maxDepth,
            int currentDepth,
            Set<String> processedVersions,
            Map<String, EnrichedDependencyDTO> allDependencies
    ) {
        if (currentDepth >= maxDepth) {
            return;
        }

        if (processedVersions.contains(versionId)) {
            return;
        }

        processedVersions.add(versionId);

        try {
            List<ModrinthVersionDTO.DependencyDTO> dependencies;
            if (dependencyType != null && !dependencyType.trim().isEmpty()) {
                dependencies = getVersionDependenciesByType(versionId, dependencyType);
            } else {
                dependencies = getVersionDependencies(versionId);
            }

            if (dependencies.isEmpty()) {
                return;
            }

            List<String> projectIds = dependencies.stream()
                    .map(ModrinthVersionDTO.DependencyDTO::getProjectId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (projectIds.isEmpty()) {
                return;
            }

            Map<String, ModrinthProjectDTO> projectsMap = getProjectsBulk(projectIds);

            for (ModrinthVersionDTO.DependencyDTO dep : dependencies) {
                String depKey = dep.getProjectId() + "_" + dep.getVersionId();

                if (!allDependencies.containsKey(depKey)) {
                    EnrichedDependencyDTO enriched = new EnrichedDependencyDTO();
                    enriched.setProjectId(dep.getProjectId());
                    enriched.setVersionId(dep.getVersionId());
                    enriched.setDependencyType(dep.getDependencyType());

                    ModrinthProjectDTO project = projectsMap.get(dep.getProjectId());
                    if (project != null) {
                        enriched.setProjectName(project.getTitle());
                        enriched.setProjectSlug(project.getSlug());
                        enriched.setIconUrl(project.getIconUrl());
                        enriched.setDescription(project.getDescription());
                    }

                    allDependencies.put(depKey, enriched);

                    if (dep.getVersionId() != null && !dep.getVersionId().trim().isEmpty()) {
                        collectTransitiveDependencies(
                                dep.getVersionId(),
                                dependencyType,
                                maxDepth,
                                currentDepth + 1,
                                processedVersions,
                                allDependencies
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar dependências de " + versionId + ": " + e.getMessage());
        }
    }

    @Override
    public List<EnrichedDependencyDTO> getProjectTransitiveDependencies(
            String projectId,
            String loader,
            String gameVersion,
            String dependencyType,
            int maxDepth
    ) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID não pode ser vazio");
        }
        if (loader == null || loader.trim().isEmpty()) {
            throw new IllegalArgumentException("Loader não pode ser vazio");
        }
        if (gameVersion == null || gameVersion.trim().isEmpty()) {
            throw new IllegalArgumentException("Game version não pode ser vazia");
        }

        List<ModrinthVersionDTO> versions = getProjectVersions(projectId, List.of(loader), List.of(gameVersion));

        if (versions == null || versions.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma versão encontrada para o projeto " + projectId);
        }

        ModrinthVersionDTO latestVersion = versions.stream()
                .filter(ver -> ver.getLoaders().contains(loader) && ver.getGameVersions().contains(gameVersion))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma versão compatível encontrada para loader '" + loader +
                        "' e game version '" + gameVersion + "'"
                ));

        return getTransitiveDependencies(latestVersion.getId(), dependencyType, maxDepth);
    }

}
