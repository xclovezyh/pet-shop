package com.petshop.repository;

import com.petshop.model.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminSessionRepository extends JpaRepository<AdminSession, Long> {
    Optional<AdminSession> findFirstByTokenHash(String tokenHash);
    void deleteByTokenHash(String tokenHash);
}
