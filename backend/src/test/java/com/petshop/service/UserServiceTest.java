package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.dto.user.VerifyCodeResponse;
import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private AppUserRepository users;

    @Mock
    private AdminAccountService adminAccountService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerShouldCreateNormalUserWhenVerificationCodeMatches() {
        VerifyCodeResponse code = userService.sendVerifyCode("13800138000");
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword("secret123");
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode(code.getCode());

        when(adminAccountService.isConfiguredAdmin("alice123")).thenReturn(false);
        when(adminAccountService.isConfiguredAdmin("alice")).thenReturn(false);
        when(users.existsByUsername("alice123")).thenReturn(false);
        when(users.existsByPhone("13800138000")).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.register(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("alice123");
        assertThat(response.getPhone()).isEqualTo("13800138000");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(users).save(any(AppUser.class));
    }

    @Test
    void registerShouldRejectConfiguredAdminUsername() {
        VerifyCodeResponse code = userService.sendVerifyCode("13800138000");
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("superadmin");
        request.setPassword("secret123");
        request.setPhone("13800138000");
        request.setNickname("normal");
        request.setCode(code.getCode());

        when(adminAccountService.isConfiguredAdmin("superadmin")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.SUPER_ADMIN_REGISTER_FORBIDDEN);

        verify(users, never()).save(any(AppUser.class));
    }

    @Test
    void loginByPasswordShouldRejectBlacklistedAccount() {
        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("bob123");
        user.setPhone("13800138001");
        user.setNickname("bob");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secret123"));
        user.setRole("USER");
        user.setBlacklisted(true);
        user.setBlacklistReason("账号违规");
        user.setCreatedAt(LocalDateTime.now());

        when(users.findByUsername("bob123")).thenReturn(Optional.of(user));

        com.petshop.dto.user.LoginByPasswordRequest request = new com.petshop.dto.user.LoginByPasswordRequest();
        request.setAccount("bob123");
        request.setPassword("secret123");

        assertThatThrownBy(() -> userService.loginByPassword(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("账号违规")
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.ACCOUNT_BLOCKED);
    }
}
