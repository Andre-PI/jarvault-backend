package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ModrinthProjectDTO {
    @JsonProperty("slug")
    private String slug;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("client_side")
    private String clientSide;

    @JsonProperty("server_side")
    private String serverSide;

    @JsonProperty("project_type")
    private String projectType;

    @JsonProperty("downloads")
    private Long downloads;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("author")
    private String author;

    @JsonProperty("versions")
    private List<String> versions;

    @JsonProperty("latest_version")
    private String latestVersion;

    @JsonProperty("dependencies")
    private List<EnrichedDependencyDTO> dependencies;

    // Construtores
    public ModrinthProjectDTO() {
    }

    // Getters e Setters
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getClientSide() {
        return clientSide;
    }

    public void setClientSide(String clientSide) {
        this.clientSide = clientSide;
    }

    public String getServerSide() {
        return serverSide;
    }

    public void setServerSide(String serverSide) {
        this.serverSide = serverSide;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public List<EnrichedDependencyDTO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<EnrichedDependencyDTO> dependencies) {
        this.dependencies = dependencies;
    }
}

