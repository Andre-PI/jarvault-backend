package com.avorio.jar_vault.dto.modrinth;

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
public class ProjectDTO {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String body;
    private String bodyUrl;
    private String status;           // "approved", "listed", etc.
    private String requestedStatus;
    private String moderatorMessage;

    private String projectType;      // "mod", "modpack", etc.
    private String team;
    private String organization;     // pode ser null no JSON

    private String clientSide;       // required | optional | unsupported | unknown
    private String serverSide;       // required | optional | unsupported | unknown

    private List<String> categories;
    private List<String> additionalCategories;
    private List<String> loaders;
    private List<String> versions;
    private List<String> gameVersions;

    private String iconUrl;
    private String issuesUrl;
    private String sourceUrl;
    private String wikiUrl;
    private String discordUrl;

    private List<String> donationUrls;
    private List<String> gallery;

    private Long downloads;
    private Long followers;

    private Integer color;           // veio como number
    private String threadId;
    private String monetizationStatus;

    private Instant published;
    private Instant updated;
    private Instant approved;
    private Instant queued;
}
