package com.avorio.jar_vault.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public  class DependencyDTO {
    private String versionId;
    private String projectId;
    private String fileName;
    private String dependencyType;

}