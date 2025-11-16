package com.avorio.jar_vault.dto.modrinth;

import com.avorio.jar_vault.dto.DependencyDTO;
import com.avorio.jar_vault.dto.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModrinthProjectInfoDTO {
    private String name;
    private String versionNumber;
    private String changelog;
    private List<DependencyDTO> dependencies;
    private List<String> gameVersions;
    private String versionType; // "release", "beta", "alpha"
    private List<String> loaders;
    private Boolean featured;
    private String status; // "listed", "archived", "draft", "unlisted", "scheduled", "unknown"
    private String requestedStatus; // "listed", "archived", "draft", "unlisted"
    private String id;
    private String projectId;
    private String authorId;
    private String datePublished; // ISO-8601 format
    private Integer downloads;
    private String changelogUrl; // Always null, legacy compatibility
    private List<FileDTO> files;

}

