package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.admin.AdminAuthSessionResponse;
import com.petshop.dto.admin.AdminCreateRequest;
import com.petshop.dto.admin.AdminLoginRequest;
import com.petshop.dto.admin.AdminPermissionOptionResponse;
import com.petshop.dto.admin.AdminUserResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.model.AdminUser;
import com.petshop.repository.AdminUserRepository;
import com.petshop.support.AdminPermission;
import com.petshop.support.AdminRole;
import com.petshop.support.PageSupport;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
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
        normalizeAdmin(adminUser);
        adminUser.setLastLoginAt(LocalDateTime.now());
        return buildSession(admins.save(adminUser));
    }

    public AdminUserResponse me(AdminUser adminUser) {
        normalizeAdmin(adminUser);
        return toResponse(adminUser);
    }

    public PageResponse<AdminUserResponse> list(Integer page, Integer size) {
        return PageSupport.slice(admins.findAll().stream()
                .peek(this::normalizeAdmin)
                .sorted((left, right) -> right.getId().compareTo(left.getId()))
                .collect(Collectors.toList()), page, size, this::toResponse);
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
        adminUser.setRole(AdminRole.ADMIN.name());
        adminUser.setPermissions(normalizePermissionCsv(request.getPermissions()));
        adminUser.setPasswordHash(PASSWORD_ENCODER.encode(password));
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        return toResponse(admins.save(adminUser));
    }

    public AdminUserResponse updateStatus(Long id, boolean enabled, AdminUser operator) {
        AdminUser adminUser = findAdmin(id);
        if (operator != null && operator.getId().equals(id) && !enabled) {
            throw new ApiException(ApiErrorCode.SELF_ROLE_REVOKE_FORBIDDEN, "不能停用当前登录的超级管理员账号");
        }
        adminUser.setEnabled(enabled);
        normalizeAdmin(adminUser);
        return toResponse(admins.save(adminUser));
    }

    public AdminUserResponse updatePermissions(Long id, List<String> permissions, AdminUser operator) {
        AdminUser adminUser = findAdmin(id);
        if (AdminRole.SUPER_ADMIN.name().equalsIgnoreCase(safe(adminUser.getRole()))) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "不能修改超级管理员的权限");
        }
        if (operator != null && operator.getId().equals(id)) {
            throw new ApiException(ApiErrorCode.SELF_ROLE_REVOKE_FORBIDDEN, "不能修改当前登录管理员自己的权限");
        }
        adminUser.setPermissions(normalizePermissionCsv(permissions));
        normalizeAdmin(adminUser);
        return toResponse(admins.save(adminUser));
    }

    public List<AdminPermissionOptionResponse> permissionOptions() {
        return Arrays.stream(AdminPermission.values())
                .map(this::toPermissionOption)
                .collect(Collectors.toList());
    }

    private AdminUser findAdmin(Long id) {
        return admins.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "管理员账号不存在"));
    }

    private AdminAuthSessionResponse buildSession(AdminUser adminUser) {
        AdminAuthSessionResponse response = new AdminAuthSessionResponse();
        response.setToken(adminSessionService.createSession(adminUser));
        response.setAdmin(toResponse(adminUser));
        return response;
    }

    private AdminUserResponse toResponse(AdminUser adminUser) {
        normalizeAdmin(adminUser);
        AdminUserResponse response = new AdminUserResponse();
        response.setId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setDisplayName(adminUser.getDisplayName());
        response.setRole(adminUser.getRole());
        response.setPermissions(AdminPermission.toCodeList(AdminPermission.parseCsv(adminUser.getPermissions())));
        response.setEnabled(adminUser.getEnabled());
        response.setCreatedAt(adminUser.getCreatedAt());
        response.setLastLoginAt(adminUser.getLastLoginAt());
        return response;
    }

    private AdminPermissionOptionResponse toPermissionOption(AdminPermission permission) {
        AdminPermissionOptionResponse response = new AdminPermissionOptionResponse();
        response.setCode(permission.getCode());
        response.setName(permission.getDisplayName());
        response.setDescription(permission.getDescription());
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
            throw new ApiException(ApiErrorCode.INVALID_PASSWORD, "密码长度需在 6-64 位");
        }
        return password;
    }

    private String normalizePermissionCsv(List<String> permissions) {
        LinkedHashSet<AdminPermission> parsed = AdminPermission.parseCodes(permissions);
        long requestedCount = permissions == null ? 0 : permissions.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .count();
        if (permissions != null && parsed.size() != requestedCount) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "存在无效的管理员权限编码");
        }
        return AdminPermission.toCsv(parsed);
    }

    private void normalizeAdmin(AdminUser adminUser) {
        if (adminUser == null) {
            return;
        }
        if (safe(adminUser.getRole()).isEmpty()) {
            adminUser.setRole(AdminRole.SUPER_ADMIN.name());
        }
        if (adminUser.getPermissions() == null) {
            adminUser.setPermissions("");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
