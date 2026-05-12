package com.petshop.support;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.model.AdminUser;

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
}
