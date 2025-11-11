package com.avorio.jar_vault.controller;


import com.avorio.jar_vault.service.JarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/jar")
public class JarController {

    private final JarService jarService;

    public JarController(JarService jarService) {
        this.jarService = jarService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJar(MultipartFile file) {
        jarService.uploadJar(file);
        return ResponseEntity.ok("Hello World");
    }
}
