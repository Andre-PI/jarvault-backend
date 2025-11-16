package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.GameVersionDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.modrinth.*;
import com.avorio.jar_vault.exception.ApiOfflineException;
import com.avorio.jar_vault.exception.RateLimitException;
import com.avorio.jar_vault.exception.ResourceNotFoundException;
import com.avorio.jar_vault.utils.JarUtil;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ModrinthServiceImpl implements ModrinthService {

    private final ModrinthUtil modrinthUtil;
    @Value("${api.modrinth.url}")
    private String modrinthApiUrl;

    private final RestTemplate restTemplate;

    public ModrinthServiceImpl(RestTemplate restTemplate, JarUtil jarUtil, ModrinthUtil modrinthUtil) {
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

        return dependencies.stream()
                .map(dep -> {
                    EnrichedDependencyDTO enriched = new EnrichedDependencyDTO();
                    enriched.setProjectId(dep.getProjectId());
                    enriched.setVersionId(dep.getVersionId());
                    enriched.setDependencyType(dep.getDependencyType());

                    // Buscar informações do projeto
                    try {
                        ModrinthProjectDTO project = getProject(dep.getProjectId());
                        enriched.setProjectSlug(project.getSlug());
                        enriched.setProjectName(project.getTitle());
                        enriched.setIconUrl(project.getIconUrl());
                        enriched.setDescription(project.getDescription());
                    } catch (Exception e) {
                        // Se falhar ao buscar o projeto, apenas deixa os campos vazios
                        enriched.setProjectSlug(dep.getProjectId());
                        enriched.setProjectName("Unknown");
                    }

                    return enriched;
                })
                .collect(Collectors.toList());
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
    public List<Categories> getCategories() {
        ResponseEntity<List<Categories>> response = restTemplate.exchange(modrinthApiUrl + "/v2/tag/category   ",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    @Override
    public List<GameVersionDTO> getMInecraftVersions() {

        ResponseEntity<List<GameVersionDTO>> response = restTemplate.exchange(
                modrinthApiUrl + "/v2/tag/game_version",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        if(response.getStatusCode().is2xxSuccessful()){
            return Objects.requireNonNull(response.getBody())
                    .stream()
                    .filter(gameVersionDTO -> gameVersionDTO.getVersionType().equals("release"))
                    .toList();
        }
        return List.of();
    }


}
