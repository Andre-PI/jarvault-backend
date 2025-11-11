package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.JarDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface JarService {
    void uploadJar(MultipartFile jarFile);
    Page<JarDTO> getJar(Long id);
    void deleteJar(Long id);

}
