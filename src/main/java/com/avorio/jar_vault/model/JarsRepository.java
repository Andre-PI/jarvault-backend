package com.avorio.jar_vault.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JarsRepository extends JpaRepository<Jars, Long> {
    Jars findByHash(String hash);

    boolean existsByHash(String hash);

    boolean existsByName(String name);

    boolean existsByProjectId(String projectId);

    Jars findByProjectId(String projectId);
}