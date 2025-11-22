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
public class ModrinthDependencyDTO {
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("version_id")
    private String versionId;
    @JsonProperty("dependency_type")
    private String dependencyType;
}

