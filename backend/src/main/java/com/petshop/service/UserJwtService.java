package com.petshop.service;

import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserJwtService {
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String TOKEN_TYPE_USER = "USER";

    private final AppUserRepository users;
    private final SecretKey secretKey;
    private final int tokenHours;

    public UserJwtService(AppUserRepository users,
                          @Value("${app.user-jwt-secret:change-me-user-jwt-secret}") String secret,
                          @Value("${app.user-jwt-hours:6}") int tokenHours) {
        this.users = users;
        this.secretKey = buildSecretKey(secret);
        this.tokenHours = Math.max(1, tokenHours);
    }

    public String createToken(AppUser user) {
        return createToken(user, LocalDateTime.now());
    }

    public String createToken(AppUser user, LocalDateTime now) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_USER)
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(expiresAt(now)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public LocalDateTime expiresAt(LocalDateTime issuedAt) {
        return issuedAt.plusHours(tokenHours);
    }

    public Optional<AppUser> resolveUser(String token) {
        if (isBlank(token)) {
            return Optional.empty();
        }
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            if (!TOKEN_TYPE_USER.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
                return Optional.empty();
            }
            Long userId = parseUserId(claims.getSubject());
            if (userId == null) {
                return Optional.empty();
            }
            Optional<AppUser> user = users.findById(userId);
            if (!user.isPresent()) {
                return Optional.empty();
            }
            AppUser appUser = user.get();
            if (!token.equals(appUser.getJwtToken())) {
                return Optional.empty();
            }
            if (appUser.getJwtTokenExpiresAt() == null || appUser.getJwtTokenExpiresAt().isBefore(LocalDateTime.now())) {
                return Optional.empty();
            }
            return user;
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private SecretKey buildSecretKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest((secret == null ? "" : secret).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Long parseUserId(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
