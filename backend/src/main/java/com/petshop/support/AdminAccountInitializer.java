package com.petshop.support;

import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class AdminAccountInitializer implements CommandLineRunner {
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AppUserRepository users;
    private final String adminUsernames;
    private final String adminPassword;

    public AdminAccountInitializer(AppUserRepository users,
                                   @Value("${app.admin-usernames:${app.admin-nicknames:superadmin}}") String adminUsernames,
                                   @Value("${app.admin-password:change-me-admin-password}") String adminPassword) {
        this.users = users;
        this.adminUsernames = adminUsernames;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Arrays.stream(adminUsernames.split(","))
                .map(String::trim)
                .filter(username -> !username.isEmpty())
                .forEach(this::ensureAdmin);
    }

    private void ensureAdmin(String username) {
        AppUser user = users.findByUsername(username)
                .orElseGet(() -> users.findByNickname(username).orElseGet(AppUser::new));
        boolean isNew = user.getId() == null;
        user.setUsername(username);
        user.setNickname(isBlank(user.getNickname()) ? username : user.getNickname());
        user.setRole(UserGuard.ROLE_SUPER_ADMIN);
        user.setBlacklisted(false);
        if (isNew || user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setPasswordSalt("");
        user.setPasswordHash(PASSWORD_ENCODER.encode(adminPassword));
        users.save(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
