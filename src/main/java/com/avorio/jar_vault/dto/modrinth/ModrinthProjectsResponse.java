package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModrinthProjectsResponse {
    private List<ProjectDTO> projects;
    private List<VersionDTO> versions;
}
