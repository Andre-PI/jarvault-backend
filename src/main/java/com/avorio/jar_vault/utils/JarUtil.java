package com.avorio.jar_vault.utils;

import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.dto.modrinth.JarModInfo;
import com.avorio.jar_vault.exception.AlgorithmErrorException;
import com.avorio.jar_vault.exception.JarAlreadyExists;
import com.avorio.jar_vault.model.Jars;
import com.avorio.jar_vault.model.JarsRepository;
import com.avorio.jar_vault.service.ModrinthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Component
public class JarUtil {
    private final ModrinthService modrinthService;
    @Value("${jar.storage.directory}")
    private  String directoryPath;

    private final JarsRepository jarsRepository;

    public JarUtil(JarsRepository jarsRepository, ModrinthService modrinthService) {
        this.jarsRepository = jarsRepository;
        this.modrinthService = modrinthService;
    }

    public Jars prepareJarModel(MultipartFile jarFile, Optional<JarModInfo> jarModInfo) {
        JarModInfo payload = jarModInfo.orElse(null);
        if(jarFile.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if(!Objects.requireNonNull(jarFile.getOriginalFilename()).endsWith(".jar")) {
            throw new IllegalArgumentException("File is not a JAR");
        }

        Jars jars = new Jars();
        jars.setHash(hashJar(jarFile));
        if(jarsRepository.existsByHash(jars.getHash())) {
            throw new JarAlreadyExists("This jar already exists in the database");
        }
        if(payload == null) {
            jars.setName(jarFile.getOriginalFilename());
            jars.setSize(jarFile.getSize());
            jars.setVersion("unknown");
            jars.setLoader("unknown");
            jars.setProjectId("unknown");
            jars.setFilePath(directoryPath + jarFile.getOriginalFilename());
            jars.setUploadedAt(Timestamp.from(Instant.now()));
            return jars;
        }
        jars.setName(jarFile.getOriginalFilename());
        jars.setSize(jarFile.getSize());
        jars.setVersion(payload.getVersion());
        jars.setLoader(payload.getLoader());
        jars.setProjectId(payload.getProjectId());
        jars.setFilePath(directoryPath + jarFile.getOriginalFilename());
        jars.setUploadedAt(Timestamp.from(Instant.now()));
        System.out.println(jars);
        return jars;
    }

    public String hashJar(MultipartFile jarFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA512");
            InputStream inputStream = jarFile.getInputStream();
            byte[] byteArray = new byte[8192];
            int bytesCount;
            while ((bytesCount = inputStream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            throw new AlgorithmErrorException("Check the algorithm name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JarDTO convertToDTO(Jars jar) {
        JarDTO jarDTO = new JarDTO();
        jarDTO.setId(jar.getId());
        jarDTO.setName(jar.getName());
        jarDTO.setVersion(jar.getVersion());
        jarDTO.setLoader(jar.getLoader());
        jarDTO.setProjectId(jar.getProjectId());
        jarDTO.setSize(jar.getSize());
        modrinthService.getProjectInfo(jarDTO.getProjectId(), jarDTO.getLoader(), jarDTO.getVersion())
                .stream()
                .findFirst()
                .ifPresent(jarDTO::setClientRequiredMods);
        return jarDTO;
    }
}
