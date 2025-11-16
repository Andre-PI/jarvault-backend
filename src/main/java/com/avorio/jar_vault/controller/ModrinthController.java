package com.avorio.jar_vault.controller;

import com.avorio.jar_vault.dto.GameVersionDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.modrinth.*;
import com.avorio.jar_vault.service.JarService;
import com.avorio.jar_vault.service.ModrinthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modrinth")
public class ModrinthController {

    private final ModrinthService modrinthService;

    public ModrinthController(ModrinthService modrinthService, JarService jarService) {
        this.modrinthService = modrinthService;
    }

    @GetMapping("/status")
    public ResponseEntity<Message> getStatus() {
        Message status = modrinthService.modrinthApiStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/search")
    public ResponseEntity<ModrinthSearchResponse> searchProjects(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> facets,
            @RequestParam(required = false, defaultValue = "relevance") String index,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        ModrinthSearchResponse response = modrinthService.searchProjects(query, facets, index, offset, limit);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/project/{projectId}")
    public ResponseEntity<ModrinthProjectDTO> getProject(@PathVariable String projectId) {
        ModrinthProjectDTO project = modrinthService.getProject(projectId);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/project/{projectId}/enriched")
    public ResponseEntity<ModrinthProjectDTO> getEnrichedProject(@PathVariable String projectId) {
        ModrinthProjectDTO project = modrinthService.getEnrichedProject(projectId);
        return ResponseEntity.ok(project);
    }


    @GetMapping("/project/{projectId}/versions")
    public ResponseEntity<List<ModrinthVersionDTO>> getProjectVersions(
            @PathVariable String projectId,
            @RequestParam(required = false) List<String> loaders,
            @RequestParam(required = false) List<String> gameVersions
    ) {
        List<ModrinthVersionDTO> versions = modrinthService.getProjectVersions(projectId, loaders, gameVersions);
        return ResponseEntity.ok(versions);
    }


    @GetMapping("/version/{versionId}")
    public ResponseEntity<ModrinthVersionDTO> getVersion(@PathVariable String versionId) {
        ModrinthVersionDTO version = modrinthService.getVersion(versionId);
        return ResponseEntity.ok(version);
    }

    @GetMapping("/version/{versionId}/dependencies")
    public ResponseEntity<List<ModrinthVersionDTO.DependencyDTO>> getVersionDependencies(
            @PathVariable String versionId
    ) {
        List<ModrinthVersionDTO.DependencyDTO> dependencies = modrinthService.getVersionDependencies(versionId);
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("/version/{versionId}/dependencies/{type}")
    public ResponseEntity<List<ModrinthVersionDTO.DependencyDTO>> getVersionDependenciesByType(
            @PathVariable String versionId,
            @PathVariable String type
    ) {
        List<ModrinthVersionDTO.DependencyDTO> dependencies = modrinthService.getVersionDependenciesByType(versionId, type);
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("/version/{versionId}/enriched")
    public ResponseEntity<EnrichedVersionDTO> getEnrichedVersion(@PathVariable String versionId) {
        EnrichedVersionDTO enrichedVersion = modrinthService.getEnrichedVersion(versionId);
        return ResponseEntity.ok(enrichedVersion);
    }

    @GetMapping("/version/{versionId}/dependencies/enriched")
    public ResponseEntity<List<EnrichedDependencyDTO>> getEnrichedDependencies(@PathVariable String versionId) {
        List<EnrichedDependencyDTO> dependencies = modrinthService.getEnrichedDependencies(versionId);
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("project/fetch-uri")
    public ResponseEntity<ModrinthProjectDTO> fetchProjectByUri(@RequestParam String projectId) {
        ModrinthProjectDTO project = modrinthService.getProject(projectId);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/tags/categories")
    public ResponseEntity<List<Categories>> getCategories() {
        return ResponseEntity.ok(modrinthService.getCategories());
    }

    @GetMapping("/tags/versions")
    public ResponseEntity<List<GameVersionDTO>> getVersionTags() {
        return ResponseEntity.ok(modrinthService.getMInecraftVersions());

    }

    @GetMapping("/project/{projectId}/info")
    public ResponseEntity<List<ModrinthProjectInfoDTO>> getProjectInfo(
            @PathVariable String projectId,
            @RequestParam(required = false) String loaders,
            @RequestParam(required = false, name = "game_versions") String gameVersions
    ) {
        System.out.printf("Received request for project info: projectId=%s, loaders=%s, gameVersions=%s%n", projectId, loaders, gameVersions);
        List<ModrinthProjectInfoDTO> projectInfo = modrinthService.getProjectInfo(projectId,loaders,gameVersions);
        return ResponseEntity.ok(projectInfo);
    }
}
