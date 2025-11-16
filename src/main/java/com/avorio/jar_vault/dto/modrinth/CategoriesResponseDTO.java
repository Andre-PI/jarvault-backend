package com.avorio.jar_vault.dto.modrinth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesResponseDTO {

    @JsonProperty("categories")
    @Singular
    public List<Categories> categories;
}
