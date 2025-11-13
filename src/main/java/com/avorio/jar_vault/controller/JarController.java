package com.avorio.jar_vault.controller;


import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.service.JarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        return ResponseEntity.ok("Jar uploaded successfully");
    }

    @GetMapping("/get/all")
    public ResponseEntity<Page<JarDTO>> getJars(@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(jarService.getAllJars(pageable));
    }

    @PostMapping("/delete")
    public ResponseEntity<Message> batchDeleteJars(@RequestBody BatchDeleteRequest request) {
        jarService.deleteBatchJars(request);
        return ResponseEntity.ok(new Message("Jars deleted successfully"));
    }
}
