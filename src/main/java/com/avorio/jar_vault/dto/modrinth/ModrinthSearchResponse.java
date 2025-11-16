package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ModrinthSearchResponse {
    @JsonProperty("hits")
    private List<ModrinthProjectDTO> hits;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("total_hits")
    private Integer totalHits;

    public ModrinthSearchResponse() {
    }

}

