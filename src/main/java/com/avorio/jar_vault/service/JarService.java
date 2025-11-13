package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface JarService {
    void uploadJar(MultipartFile jarFile);
    JarDTO getJar(Long id);
    Page<JarDTO> getAllJars(Pageable pageable);
    void deleteBatchJars(BatchDeleteRequest request);
}
