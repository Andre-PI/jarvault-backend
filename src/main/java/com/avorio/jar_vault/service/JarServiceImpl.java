package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.model.Jars;
import com.avorio.jar_vault.model.JarsRepository;
import com.avorio.jar_vault.utils.JarUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class JarServiceImpl implements JarService{

    private final JarUtil jarUtil;
    private final JarsRepository jarsRepository;

    @Value("${password.key}")
    private String adminKey;

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
    public JarDTO getJar(Long id) {
        return null;
    }

    public Page<JarDTO> getAllJars(Pageable pageable) {
        return jarsRepository.findAll(pageable).map(jarUtil::convertToDTO);
    }

    @Override
    public void deleteBatchJars(BatchDeleteRequest request){
        if(!adminKey.equals(request.getPasswordKey())) {
            throw new SecurityException("Invalid password key");
        }
        for(Long id : request.getListOfIds()) {
            Jars jar = jarsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Jar with id " + id + " not found"));
            Path filePath = Paths.get(jar.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        jarsRepository.deleteAllById(Arrays.asList(request.getListOfIds()));
    }
}
