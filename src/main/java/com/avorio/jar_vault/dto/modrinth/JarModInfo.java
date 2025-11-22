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
public class JarModInfo {
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("version")
    private  String version;
    @JsonProperty("loader")
    private String loader;
}
