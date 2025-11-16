package com.avorio.jar_vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameVersionDTO {
    @JsonProperty("version")
    private String version;

    @JsonProperty("version_type")
    private String versionType;

    @JsonProperty("date")
    private String date;

    @JsonProperty("major")
    private Boolean major;
}
