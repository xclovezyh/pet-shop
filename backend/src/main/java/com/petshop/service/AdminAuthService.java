package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.admin.AdminAuthSessionResponse;
import com.petshop.dto.admin.AdminCreateRequest;
import com.petshop.dto.admin.AdminLoginRequest;
import com.petshop.dto.admin.AdminUserResponse;
import com.petshop.model.AdminUser;
import com.petshop.repository.AdminUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminAuthService {
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,19}$");

    private final AdminUserRepository admins;
    private final AdminSessionService adminSessionService;

    public AdminAuthService(AdminUserRepository admins, AdminSessionService adminSessionService) {
        this.admins = admins;
        this.adminSessionService = adminSessionService;
    }

    public AdminAuthSessionResponse login(AdminLoginRequest request) {
        String username = safe(request.getUsername());
        String password = safe(request.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "请输入管理员账号和密码");
        }
        AdminUser adminUser = admins.findByUsername(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.LOGIN_FAILED, "管理员账号或密码错误"));
        if (Boolean.FALSE.equals(adminUser.getEnabled())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "管理员账号已停用");
        }
        if (!PASSWORD_ENCODER.matches(password, safe(adminUser.getPasswordHash()))) {
            throw new ApiException(ApiErrorCode.LOGIN_FAILED, "管理员账号或密码错误");
        }
        adminUser.setLastLoginAt(LocalDateTime.now());
        return buildSession(admins.save(adminUser));
    }

    public AdminUserResponse me(AdminUser adminUser) {
        return toResponse(adminUser);
    }

    public List<AdminUserResponse> list() {
        return admins.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AdminUserResponse create(AdminCreateRequest request) {
        String username = normalizeUsername(request.getUsername());
        String password = requirePassword(request.getPassword());
        String displayName = safe(request.getDisplayName());
        if (displayName.isEmpty()) {
            displayName = username;
        }
        if (admins.existsByUsername(username)) {
            throw new ApiException(ApiErrorCode.USERNAME_ALREADY_EXISTS, "管理员账号已存在");
        }
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setDisplayName(displayName);
        adminUser.setPasswordHash(PASSWORD_ENCODER.encode(password));
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        return toResponse(admins.save(adminUser));
    }

    public AdminUserResponse updateStatus(Long id, boolean enabled) {
        AdminUser adminUser = admins.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "管理员账号不存在"));
        adminUser.setEnabled(enabled);
        return toResponse(admins.save(adminUser));
    }

    private AdminAuthSessionResponse buildSession(AdminUser adminUser) {
        AdminAuthSessionResponse response = new AdminAuthSessionResponse();
        response.setToken(adminSessionService.createSession(adminUser));
        response.setAdmin(toResponse(adminUser));
        return response;
    }

    private AdminUserResponse toResponse(AdminUser adminUser) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setDisplayName(adminUser.getDisplayName());
        response.setEnabled(adminUser.getEnabled());
        response.setCreatedAt(adminUser.getCreatedAt());
        response.setLastLoginAt(adminUser.getLastLoginAt());
        return response;
    }

    private String normalizeUsername(String value) {
        String username = safe(value);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ApiException(ApiErrorCode.INVALID_USERNAME);
        }
        return username;
    }

    private String requirePassword(String value) {
        String password = safe(value);
        if (password.length() < 6 || password.length() > 64) {
            throw new ApiException(ApiErrorCode.INVALID_PASSWORD, "密码长度需为 6-64 位");
        }
        return password;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
