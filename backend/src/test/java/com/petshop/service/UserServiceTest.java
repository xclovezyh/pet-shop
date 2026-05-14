package com.petshop.service;

import com.petshop.dto.user.AuthSessionResponse;
import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.user.LoginByPasswordRequest;
import com.petshop.dto.user.RegisterUserRequest;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final String PASSWORD_DIGEST = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Mock
    private AppUserRepository users;

    @Mock
    private UserJwtService userJwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerShouldCreateNormalUserWhenVerificationCodeMatches() {
        VerifyCodeResponse code = userService.sendVerifyCode("13800138000");
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword(PASSWORD_DIGEST);
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode(code.getCode());

        when(users.existsByUsername("alice123")).thenReturn(false);
        when(users.existsByPhone("13800138000")).thenReturn(false);
        when(users.existsByNickname("alice")).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(userJwtService.createToken(any(AppUser.class), any(LocalDateTime.class))).thenReturn("token-1");
        when(userJwtService.expiresAt(any(LocalDateTime.class))).thenReturn(LocalDateTime.now().plusHours(6));

        AuthSessionResponse response = userService.register(request);

        assertThat(response.getToken()).isEqualTo("token-1");
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getUsername()).isEqualTo("alice123");
        assertThat(response.getUser().getPhone()).isEqualTo("13800138000");
        assertThat(response.getUser().getRole()).isEqualTo("USER");
        verify(users, times(2)).save(any(AppUser.class));
    }

    @Test
    void registerShouldRejectDuplicateNickname() {
        VerifyCodeResponse code = userService.sendVerifyCode("13800138000");
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword(PASSWORD_DIGEST);
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode(code.getCode());

        when(users.existsByUsername("alice123")).thenReturn(false);
        when(users.existsByPhone("13800138000")).thenReturn(false);
        when(users.existsByNickname("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("昵称已被使用，请换一个昵称");

        verify(users, never()).save(any(AppUser.class));
    }

    @Test
    void loginByPasswordShouldUpgradeLegacyDigestHashWhenSaltIsBlank() {
        AppUser user = new AppUser();
        user.setId(4L);
        user.setUsername("legacy123");
        user.setPhone("13800138004");
        user.setNickname("legacy");
        user.setPasswordSalt("");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(PASSWORD_DIGEST));
        user.setRole("USER");
        user.setBlacklisted(false);

        when(users.findByUsername("legacy123")).thenReturn(Optional.of(user));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userJwtService.createToken(any(AppUser.class), any(LocalDateTime.class))).thenReturn("token-legacy");
        when(userJwtService.expiresAt(any(LocalDateTime.class))).thenReturn(LocalDateTime.now().plusHours(6));

        LoginByPasswordRequest request = new LoginByPasswordRequest();
        request.setAccount("legacy123");
        request.setPassword(PASSWORD_DIGEST);

        AuthSessionResponse response = userService.loginByPassword(request);

        assertThat(response.getToken()).isEqualTo("token-legacy");
        assertThat(user.getPasswordSalt()).isNotBlank();
        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches(user.getPasswordSalt() + ":" + PASSWORD_DIGEST, user.getPasswordHash())).isTrue();
    }

    @Test
    void loginByPasswordShouldRejectBlacklistedAccount() {
        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("bob123");
        user.setPhone("13800138001");
        user.setNickname("bob");
        user.setPasswordSalt("salt-1");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("salt-1:" + PASSWORD_DIGEST));
        user.setRole("USER");
        user.setBlacklisted(true);
        user.setBlacklistReason("账号违规");
        user.setCreatedAt(LocalDateTime.now());

        when(users.findByUsername("bob123")).thenReturn(Optional.of(user));

        com.petshop.dto.user.LoginByPasswordRequest request = new com.petshop.dto.user.LoginByPasswordRequest();
        request.setAccount("bob123");
        request.setPassword(PASSWORD_DIGEST);

        assertThatThrownBy(() -> userService.loginByPassword(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("账号违规")
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.ACCOUNT_BLOCKED);
    }

    @Test
    void loginByPasswordShouldRejectSuperAdminAccount() {
        AppUser user = new AppUser();
        user.setId(3L);
        user.setUsername("superadmin");
        user.setPhone("13800138009");
        user.setNickname("superadmin");
        user.setPasswordSalt("salt-2");
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("salt-2:" + PASSWORD_DIGEST));
        user.setRole("SUPER_ADMIN");
        user.setBlacklisted(false);

        when(users.findByUsername("superadmin")).thenReturn(Optional.of(user));

        com.petshop.dto.user.LoginByPasswordRequest request = new com.petshop.dto.user.LoginByPasswordRequest();
        request.setAccount("superadmin");
        request.setPassword(PASSWORD_DIGEST);

        assertThatThrownBy(() -> userService.loginByPassword(request))
                .isInstanceOf(ApiException.class)
                .hasMessage(ApiErrorCode.LOGIN_FAILED.getMessage())
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.LOGIN_FAILED);

        verify(users, never()).save(any(AppUser.class));
    }
}
