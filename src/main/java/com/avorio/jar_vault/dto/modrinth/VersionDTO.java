package com.avorio.jar_vault.dto.modrinth;

import com.avorio.jar_vault.dto.FileDTO;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VersionDTO {
    private String id;
    private String projectId;
    private String authorId;
    private String name;
    private String versionNumber;
    private String changelog;
    private String changelogUrl;
    private String datePublished;
    private Instant datePublishedInstant; // opcional — se preferir Instant direto, renomeie e ajuste desserialização
    private Long downloads;
    private String versionType;
    private String status;
    private Boolean featured;
    private List<String> gameVersions;
    private List<String> loaders;
    private List<FileDTO> files;
}
