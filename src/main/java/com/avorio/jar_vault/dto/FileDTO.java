package com.avorio.jar_vault.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private Map<String, String> hashes; // e.g., {"sha512": "...", "sha1": "..."}
    private String url;
    private String filename;
    private Boolean primary;
    private Integer size;
    private String fileType; // "required-resource-pack", "optional-resource-pack"

}