package com.petshop.support;

import com.petshop.repository.AdminUserRepository;
import com.petshop.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminBootstrapInitializerTest {
    @Test
    void runShouldRejectDefaultAdminPasswordInProdWhenBootstrapping() {
        AdminUserRepository adminUsers = mock(AdminUserRepository.class);
        AppUserRepository users = mock(AppUserRepository.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(adminUsers.count()).thenReturn(0L);

        AdminBootstrapInitializer initializer = new AdminBootstrapInitializer(
                adminUsers,
                users,
                "change-me-admin-password",
                environment);

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_ADMIN_PASSWORD");
    }
}
