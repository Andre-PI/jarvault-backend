package com.avorio.jar_vault.exception;

public class JarAlreadyExists extends RuntimeException {
    public JarAlreadyExists(String message) {
        super(message);
    }
}
