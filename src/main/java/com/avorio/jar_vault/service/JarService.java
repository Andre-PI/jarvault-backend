package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.dto.UploadPayload;
import com.avorio.jar_vault.dto.modrinth.JarModInfo;
import org.apache.tomcat.Jar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface JarService {
    UploadPayload checkJarLIst(List<String> modName);

    void isJarInDatabase(String project, String loaders, String gameVersions);
    void isJarInDatabase(String hash);
    void uploadJar(MultipartFile jarFile, JarModInfo jarModInfo);
    UploadPayload uploadBatchJars(List<MultipartFile> jarFiles, List<JarModInfo> jarModInfo);
    JarDTO getJar(Long id);
    Page<JarDTO> getAllJars(Pageable pageable);
    void deleteBatchJars(BatchDeleteRequest request);
}
