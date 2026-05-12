package com.petshop.support;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.model.AdminUser;

import java.util.Set;

public final class AdminGuard {
    private AdminGuard() {
    }

    public static AdminUser requireAuthenticated(AdminUser adminUser, String action) {
        if (adminUser == null) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录管理员账号后再" + action);
        }
        if (Boolean.FALSE.equals(adminUser.getEnabled())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "管理员账号已停用");
        }
        return adminUser;
    }

    public static AdminUser requireSuperAdmin(AdminUser adminUser, String action) {
        AdminUser current = requireAuthenticated(adminUser, action);
        if (!isSuperAdmin(current)) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "只有超级管理员可以" + action);
        }
        return current;
    }

    public static AdminUser requirePermission(AdminUser adminUser, AdminPermission permission, String action) {
        AdminUser current = requireAuthenticated(adminUser, action);
        if (isSuperAdmin(current)) {
            return current;
        }
        if (!permissionSet(current).contains(permission)) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "当前管理员没有权限执行：" + action);
        }
        return current;
    }

    public static boolean isSuperAdmin(AdminUser adminUser) {
        return adminUser != null && AdminRole.SUPER_ADMIN.name().equalsIgnoreCase(safe(adminUser.getRole()));
    }

    public static Set<AdminPermission> permissionSet(AdminUser adminUser) {
        return AdminPermission.parseCsv(adminUser == null ? "" : adminUser.getPermissions());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
