package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.model.Jars;
import com.avorio.jar_vault.model.JarsRepository;
import com.avorio.jar_vault.utils.JarUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class JarServiceImpl implements JarService{

    private final JarUtil jarUtil;
    private final JarsRepository jarsRepository;

    @Value("${jar.storage.directory}")
    private String directoryPath;

    public JarServiceImpl(JarUtil jarUtil, JarsRepository jarsRepository) {
        this.jarUtil = jarUtil;
        this.jarsRepository = jarsRepository;
    }

    @Override
    public void uploadJar(MultipartFile jarFile) {
        Jars jar = jarUtil.prepareJarModel(jarFile);
        Path directoryPathObj = Paths.get(directoryPath);
        try {
            if (!Files.exists(directoryPathObj)) {
                Files.createDirectories(directoryPathObj);
            }

            Path filePath = directoryPathObj.resolve(Objects.requireNonNull(jarFile.getOriginalFilename()));
            jarFile.transferTo(filePath.toFile());

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        jarsRepository.save(jar);
    }


    @Override
    public Page<JarDTO> getJar(Long id) {
        return null;
    }

    @Override
    public void deleteJar(Long id) {

    }
}
