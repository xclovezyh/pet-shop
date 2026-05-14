package com.petshop.support;

import com.petshop.model.AdminUser;
import com.petshop.model.AppUser;
import com.petshop.repository.AdminUserRepository;
import com.petshop.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class AdminBootstrapInitializer implements CommandLineRunner {
    private static final String DEFAULT_ADMIN_PASSWORD = "change-me-admin-password";
    private static final String DEFAULT_ADMIN_HASH = "$2a$10$yiHbcFKSqzWSInAwI/JHQu/SfQnAKXi9g91hS94eL.9rKsnq9vVja";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AdminUserRepository adminUsers;
    private final AppUserRepository users;
    private final String adminPassword;
    private final Environment environment;

    public AdminBootstrapInitializer(AdminUserRepository adminUsers,
                                     AppUserRepository users,
                                     @Value("${app.admin-password:change-me-admin-password}") String adminPassword,
                                     Environment environment) {
        this.adminUsers = adminUsers;
        this.users = users;
        this.adminPassword = adminPassword;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        if (adminUsers.count() > 0) {
            return;
        }
        List<AppUser> legacyAdmins = users.findAll();
        for (AppUser legacyAdmin : legacyAdmins) {
            if (!UserGuard.ROLE_SUPER_ADMIN.equals(legacyAdmin.getRole()) || isBlank(legacyAdmin.getUsername())) {
                continue;
            }
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername(legacyAdmin.getUsername());
            adminUser.setDisplayName(isBlank(legacyAdmin.getNickname()) ? legacyAdmin.getUsername() : legacyAdmin.getNickname());
            adminUser.setRole(AdminRole.SUPER_ADMIN.name());
            adminUser.setPermissions("");
            adminUser.setPasswordHash(legacyAdmin.getPasswordHash());
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(legacyAdmin.getCreatedAt() == null ? LocalDateTime.now() : legacyAdmin.getCreatedAt());
            adminUser.setLastLoginAt(legacyAdmin.getLastLoginAt());
            adminUsers.save(adminUser);
        }
        if (adminUsers.count() == 0) {
            rejectDefaultPasswordInProd();
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername("superadmin");
            adminUser.setDisplayName("superadmin");
            adminUser.setRole(AdminRole.SUPER_ADMIN.name());
            adminUser.setPermissions("");
            adminUser.setPasswordHash(defaultAdminPasswordHash());
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUsers.save(adminUser);
        }
    }

    private String defaultAdminPasswordHash() {
        if (DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            return DEFAULT_ADMIN_HASH;
        }
        return PASSWORD_ENCODER.encode(adminPassword);
    }

    private void rejectDefaultPasswordInProd() {
        String[] profiles = environment == null ? new String[0] : environment.getActiveProfiles();
        boolean prod = Arrays.stream(profiles == null ? new String[0] : profiles)
                .anyMatch(profile -> "prod".equalsIgnoreCase(profile));
        if (prod && (isBlank(adminPassword) || DEFAULT_ADMIN_PASSWORD.equals(adminPassword.trim()))) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD must be set to a strong non-default value in prod");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
