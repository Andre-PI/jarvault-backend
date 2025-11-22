package com.avorio.jar_vault.controller;


import com.avorio.jar_vault.dto.BatchDeleteRequest;
import com.avorio.jar_vault.dto.JarDTO;
import com.avorio.jar_vault.dto.Message;
import com.avorio.jar_vault.dto.UploadPayload;
import com.avorio.jar_vault.dto.modrinth.JarModInfo;
import com.avorio.jar_vault.service.JarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/jar")
public class JarController {

    private final JarService jarService;

    public JarController(JarService jarService) {
        this.jarService = jarService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJar(MultipartFile file, @RequestBody(required = false)JarModInfo jarModInfo) {
        jarService.uploadJar(file, jarModInfo);
        return ResponseEntity.ok("Jar uploaded successfully");
    }

    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    public ResponseEntity<UploadPayload> uploadBatchJars(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "jarModInfo", required = false) String jarModInfoJson
    ) {
        List<JarModInfo> jarModInfo = null;
        if (jarModInfoJson != null && !jarModInfoJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                jarModInfo = java.util.Arrays.asList(mapper.readValue(jarModInfoJson, JarModInfo[].class));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new UploadPayload(null, new Message("Invalid jarModInfo JSON: " + e.getMessage())));
            }
        }
        UploadPayload results = jarService.uploadBatchJars(files, jarModInfo);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/check")
    public ResponseEntity<Message> checkJarExists(@RequestParam String modName, @RequestParam(required = false) String loaders, @RequestParam(required = false) String gameVersions) {
        jarService.isJarInDatabase(modName, loaders, gameVersions);
        return ResponseEntity.ok(new Message("Jar not uploaded yet"));
    }

    @GetMapping("/check/{hash}")
    public ResponseEntity<Message> checkJarByHash(@PathVariable String hash) {
        jarService.isJarInDatabase(hash);
        return ResponseEntity.ok(new Message("Jar not uploaded yet"));
    }

    @PostMapping("/check/batch")
    public ResponseEntity<UploadPayload> checkBatchJarsExist(@RequestBody List<String> modNames) {

        return ResponseEntity.ok(jarService.checkJarLIst(modNames));
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
