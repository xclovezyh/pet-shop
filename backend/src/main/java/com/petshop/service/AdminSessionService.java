package com.petshop.service;

import com.petshop.model.AdminSession;
import com.petshop.model.AdminUser;
import com.petshop.repository.AdminSessionRepository;
import com.petshop.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AdminSessionService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AdminSessionRepository sessions;
    private final AdminUserRepository admins;
    private final int sessionDays;

    public AdminSessionService(AdminSessionRepository sessions,
                               AdminUserRepository admins,
                               @Value("${app.auth-session-days:30}") int sessionDays) {
        this.sessions = sessions;
        this.admins = admins;
        this.sessionDays = sessionDays;
    }

    public String createSession(AdminUser adminUser) {
        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();

        AdminSession session = new AdminSession();
        session.setAdminUserId(adminUser.getId());
        session.setTokenHash(hashToken(token));
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plusDays(Math.max(1, sessionDays)));
        sessions.save(session);
        return token;
    }

    public Optional<AdminUser> resolveAdmin(String token) {
        if (isBlank(token)) {
            return Optional.empty();
        }
        Optional<AdminSession> sessionOptional = sessions.findFirstByTokenHash(hashToken(token));
        if (!sessionOptional.isPresent()) {
            return Optional.empty();
        }
        AdminSession session = sessionOptional.get();
        if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessions.delete(session);
            return Optional.empty();
        }
        session.setLastUsedAt(LocalDateTime.now());
        sessions.save(session);
        return admins.findById(session.getAdminUserId());
    }

    public void invalidate(String token) {
        if (isBlank(token)) {
            return;
        }
        sessions.deleteByTokenHash(hashToken(token));
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
