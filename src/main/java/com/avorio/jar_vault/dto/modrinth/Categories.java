package com.avorio.jar_vault.dto.modrinth;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Categories {
    @JsonProperty("icon")
    String icon;
    @JsonProperty("name")
    String name;
    @JsonProperty("project_type")
    String projectType;
    @JsonProperty("header")
    String header;
}
