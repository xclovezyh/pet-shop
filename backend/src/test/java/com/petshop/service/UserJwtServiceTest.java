package com.petshop.service;

import com.petshop.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserJwtServiceTest {
    @Test
    void constructorShouldRejectDefaultSecretInProdProfile() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        assertThatThrownBy(() -> new UserJwtService(mock(AppUserRepository.class),
                "change-me-user-jwt-secret",
                6,
                environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_USER_JWT_SECRET");
    }
}
