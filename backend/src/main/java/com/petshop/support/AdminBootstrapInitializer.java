package com.petshop.support;

import com.petshop.model.AdminUser;
import com.petshop.model.AppUser;
import com.petshop.repository.AdminUserRepository;
import com.petshop.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AdminBootstrapInitializer implements CommandLineRunner {
    private static final String DEFAULT_ADMIN_HASH = "$2a$10$yiHbcFKSqzWSInAwI/JHQu/SfQnAKXi9g91hS94eL.9rKsnq9vVja";

    private final AdminUserRepository adminUsers;
    private final AppUserRepository users;

    public AdminBootstrapInitializer(AdminUserRepository adminUsers, AppUserRepository users) {
        this.adminUsers = adminUsers;
        this.users = users;
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
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername("superadmin");
            adminUser.setDisplayName("superadmin");
            adminUser.setRole(AdminRole.SUPER_ADMIN.name());
            adminUser.setPermissions("");
            adminUser.setPasswordHash(DEFAULT_ADMIN_HASH);
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUsers.save(adminUser);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
