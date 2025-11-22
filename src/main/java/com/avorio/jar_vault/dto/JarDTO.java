package com.avorio.jar_vault.dto;

import com.avorio.jar_vault.dto.modrinth.ModrinthProjectInfoDTO;
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
    private String projectId;
    private String loader;
    private ModrinthProjectInfoDTO clientRequiredMods;
}
