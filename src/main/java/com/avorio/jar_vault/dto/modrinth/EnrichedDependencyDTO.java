package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedDependencyDTO {
    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("project_slug")
    private String projectSlug;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("version_id")
    private String versionId;

    @JsonProperty("dependency_type")
    private String dependencyType;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("description")
    private String description;
}

