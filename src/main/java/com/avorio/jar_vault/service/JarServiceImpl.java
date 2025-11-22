package com.avorio.jar_vault.service;

import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.UploadPayload;
import com.avorio.jar_vault.dto.modrinth.JarModInfo;
import com.avorio.jar_vault.dto.modrinth.ModrinthProjectInfoDTO;
import com.avorio.jar_vault.exception.JarAlreadyExists;
import com.avorio.jar_vault.model.Jars;
import com.avorio.jar_vault.model.JarsRepository;
import com.avorio.jar_vault.utils.JarUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JarServiceImpl implements JarService{

    private final JarUtil jarUtil;
    private final JarsRepository jarsRepository;
    private final ModrinthService modrinthService;

    @Value("${password.key}")
    private String adminKey;

    @Value("${jar.storage.directory}")
    private String directoryPath;


    public JarServiceImpl(JarUtil jarUtil, JarsRepository jarsRepository, ModrinthService modrinthService) {
        this.jarUtil = jarUtil;
        this.jarsRepository = jarsRepository;
        this.modrinthService = modrinthService;
    }

    @Override
    public UploadPayload checkJarLIst(List<String> modName) {
        UploadPayload uploadPayload = new UploadPayload();
        Message errorMessage = new Message();
        for(String name : modName) {
            if(jarsRepository.existsByName(name)) {
                errorMessage.setMessage("Jar " + name +  " already exists in the database.");
                uploadPayload.setFailure(errorMessage);
            }else{
                Message successMessage = new Message();
                successMessage.setMessage("Jar " + name + " does not exist in the database.");
                uploadPayload.setSuccess(successMessage);
            }
        }
        return uploadPayload;
    }

    @Override
    public void isJarInDatabase(String project, String loaders, String gameVersions) {
        List<ModrinthProjectInfoDTO> projectInfo = modrinthService.getProjectInfo(project, loaders,gameVersions);

        for(ModrinthProjectInfoDTO info : projectInfo) {;
            String hash = info.getFiles()
                    .stream()
                    .filter(fileDTO -> fileDTO.getHashes().get("sha512") != null)
                    .map(fileDTO -> fileDTO.getHashes().get("sha512"))
                    .findFirst()
                    .orElse("");
            if (jarsRepository.existsByHash(hash)) {
                throw new JarAlreadyExists("Jar " + info.getName() + " already exists in the database.");
            }
        }
    }

    @Override
    public void isJarInDatabase(String hash) {
        if (jarsRepository.existsByHash(hash)) {
            throw new JarAlreadyExists("Jar with hash " + hash + " already exists in the database.");
        }
    }

    @Override
    public void uploadJar(MultipartFile jarFile, JarModInfo jarModInfo) {
        Jars jar = jarUtil.prepareJarModel(jarFile, java.util.Optional.ofNullable(jarModInfo));
        if(jarsRepository.existsByHash(jar.getHash())) {
            throw new JarAlreadyExists("This jar already exists in the database");
        }
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
    public UploadPayload uploadBatchJars(List<MultipartFile> jarFiles, List<JarModInfo> jarModInfo) {
        Path directoryPathObj = Paths.get(directoryPath);

        try {
            if (!Files.exists(directoryPathObj)) {
                Files.createDirectories(directoryPathObj);
            }
        } catch (IOException ioException) {
            throw new RuntimeException("Failed to create directory: " + ioException.getMessage());
        }
        UploadPayload uploadPayload = new UploadPayload();
        Message errorMessage = new Message();
        Message successMessage = new Message();
        for (MultipartFile jarFile : jarFiles) {
            try {
                Jars jar = jarUtil.prepareJarModel(jarFile, jarModInfo.stream().filter(file -> file.getFileName().equals(jarFile.getOriginalFilename())).findFirst());
                if(jarsRepository.existsByHash(jar.getHash())) {
                    errorMessage.setMessage("Jar " + jar.getName() +  " already exists. Skipping upload.");
                    uploadPayload.setFailure(errorMessage);
                    continue;
                }
                Path filePath = directoryPathObj.resolve(Objects.requireNonNull(jarFile.getOriginalFilename()));
                jarFile.transferTo(filePath.toFile());
                jarsRepository.save(jar);
                successMessage.setMessage("Jar " + jar.getName() + " uploaded successfully.");
                uploadPayload.setSuccess(successMessage);
            } catch (Exception e) {
                ;
                uploadPayload.setFailure(new Message("Failed to upload jar " + jarFile.getOriginalFilename() + ": " + e.getMessage()));
            }
        }

        return uploadPayload;
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
