package com.avorio.jar_vault.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JarsRepository extends JpaRepository<Jars, Long> {
    Jars findByHash(String hash);

    boolean existsByHash(String hash);
}