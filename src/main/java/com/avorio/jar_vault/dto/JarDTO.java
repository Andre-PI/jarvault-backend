package com.avorio.jar_vault.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JarDTO {

    private String name;
    private String version;
    private String hash;
    private String filePath;
    private Long size;
}
