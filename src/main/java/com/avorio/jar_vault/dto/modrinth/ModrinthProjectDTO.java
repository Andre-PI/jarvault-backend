package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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

    @JsonProperty("is_added")
    private Boolean isAdded;

    // Construtores
    public ModrinthProjectDTO() {
    }

}

