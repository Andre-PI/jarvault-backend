package com.avorio.jar_vault.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity(name = "jars")
@Getter
@Setter
public class Jars {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "version")
    private String version;

    @Column(nullable = false, name = "jar_hash", unique = true)
    private String hash;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "uploaded_at", nullable = false)
    private Timestamp uploadedAt;

    @Column(name = "size_bytes", nullable = false)
    private Long size;


}
