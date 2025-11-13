package com.avorio.jar_vault.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JarDTO {

    private  Long id;
    private String name;
    private String version;
    private Long size;
}
