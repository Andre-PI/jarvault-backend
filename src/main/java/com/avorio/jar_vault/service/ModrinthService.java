package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.GameVersionDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.modrinth.ModrinthProjectInfoDTO;
import com.avorio.jar_vault.dto.modrinth.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ModrinthService {
    Message modrinthApiStatus();

    ModrinthSearchResponse searchProjects(String query, List<String> facets, String index, Integer offset, Integer limit);

    ModrinthProjectDTO getProject(String projectId);

    List<ModrinthProjectInfoDTO> getProjectInfo(String projectId, String loaders, String gameVersions);

    List<ModrinthProjectDTO> getProjectsInBulk(List<String> projectIds);

    List<ModrinthVersionDTO> getProjectVersions(String projectId, List<String> loaders, List<String> gameVersions);

    ModrinthVersionDTO getVersion(String versionId);

    List<ModrinthVersionDTO.DependencyDTO> getVersionDependencies(String versionId);

    List<ModrinthVersionDTO.DependencyDTO> getVersionDependenciesByType(String versionId, String dependencyType);

    EnrichedVersionDTO getEnrichedVersion(String versionId);

    List<EnrichedDependencyDTO> getEnrichedDependencies(String versionId);

    ModrinthProjectDTO getEnrichedProject(String projectId);

    List<Categories> getCategories();

    List<GameVersionDTO> getMInecraftVersions();

    ModrinthProjectsResponse getDependenciesFromProject(String projectId);

    List<ModrinthVersionDTO> getClientDependenciesFromProject(String projectId, String loader, String gameVersion);

    Map<String, ModrinthProjectDTO> getProjectsBulk(List<String> projectIds);

    List<EnrichedDependencyDTO> getTransitiveDependencies(String versionId, String dependencyType, int maxDepth);

    List<EnrichedDependencyDTO> getProjectTransitiveDependencies(String projectId, String loader, String gameVersion, String dependencyType, int maxDepth);
}
