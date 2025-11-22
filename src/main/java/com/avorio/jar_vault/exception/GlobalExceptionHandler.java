package com.avorio.jar_vault.exception;


import com.avorio.jar_vault.dto.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.NoSuchAlgorithmException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Message> handleAllExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body(new Message(ex.getMessage()));
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    public ResponseEntity<Message> handleNoSuchAlgorithmException(NoSuchAlgorithmException ex) {
        return ResponseEntity.internalServerError().body(new Message("Algorithm error: " + ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Message> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.internalServerError().body(new Message("Error in hashing: " + ex.getMessage()));
    }

    @ExceptionHandler(JarAlreadyExists.class)
    public ResponseEntity<Message> handleJarAlreadyExists(JarAlreadyExists ex) {
        return ResponseEntity.badRequest().body(new Message("Jar already exists: " + ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Message> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(403).body(new Message("Security error: " + ex.getMessage()));
    }

    @ExceptionHandler(ApiOfflineException.class)
    public ResponseEntity<Message> handleApiOfflineException(ApiOfflineException ex) {
        return ResponseEntity.status(503).body(new Message("Modrinth API is offline: " + ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Message> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new Message("Resource not found: " + ex.getMessage()));
    }
}
