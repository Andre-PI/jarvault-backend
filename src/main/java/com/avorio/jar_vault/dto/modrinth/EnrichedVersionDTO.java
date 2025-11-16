package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnrichedVersionDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version_number")
    private String versionNumber;

    @JsonProperty("changelog")
    private String changelog;

    @JsonProperty("dependencies")
    private List<EnrichedDependencyDTO> dependencies;

    @JsonProperty("game_versions")
    private List<String> gameVersions;

    @JsonProperty("version_type")
    private String versionType;

    @JsonProperty("loaders")
    private List<String> loaders;

    @JsonProperty("featured")
    private Boolean featured;

    @JsonProperty("downloads")
    private Long downloads;

    @JsonProperty("date_published")
    private String datePublished;

    @JsonProperty("files")
    private List<ModrinthVersionDTO.FileDTO> files;

    public EnrichedVersionDTO() {
    }
}

