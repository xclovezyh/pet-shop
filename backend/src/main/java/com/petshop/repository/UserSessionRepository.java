package com.petshop.repository;

import com.petshop.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findFirstByTokenHash(String tokenHash);
    Optional<UserSession> findFirstByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime expiresAt);
    void deleteByTokenHash(String tokenHash);
}
