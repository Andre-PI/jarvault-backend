package com.avorio.jar_vault.utils;

import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.exception.AlgorithmErrorException;
import com.avorio.jar_vault.exception.JarAlreadyExists;
import com.avorio.jar_vault.model.Jars;
import com.avorio.jar_vault.model.JarsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import static javax.xml.crypto.dsig.DigestMethod.SHA256;



@Component
public class JarUtil {
    @Value("${jar.storage.directory}")
    private  String directoryPath;

    private final JarsRepository jarsRepository;

    public JarUtil(JarsRepository jarsRepository) {
        this.jarsRepository = jarsRepository;
    }

    public Jars prepareJarModel(MultipartFile jarFile) {
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
        jars.setName(jarFile.getOriginalFilename());
        jars.setSize(jarFile.getSize());
        jars.setVersion("1.20.1");
        jars.setFilePath(directoryPath + jarFile.getOriginalFilename());
        jars.setUploadedAt(Timestamp.from(Instant.now()));
        return jars;
    }

    private String hashJar(MultipartFile jarFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA256");
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
        jarDTO.setSize(jar.getSize());
        return jarDTO;
    }
}
