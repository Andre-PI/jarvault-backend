package com.avorio.jar_vault.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.NoSuchAlgorithmException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    public ResponseEntity<String> handleNoSuchAlgorithmException(NoSuchAlgorithmException ex) {
        return ResponseEntity.internalServerError().body("Algorithm error: " + ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.internalServerError().body("Error in hashing: " + ex.getMessage());
    }

    @ExceptionHandler(JarAlreadyExists.class)
    public ResponseEntity<String> handleJarAlreadyExists(JarAlreadyExists ex) {
        return ResponseEntity.badRequest().body("Jar already exists: " + ex.getMessage());
    }

}
