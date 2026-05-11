package com.petshop.support;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;

public final class UserGuard {
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private UserGuard() {
    }

    public static AppUser requireActive(AppUserRepository users, String nickname, String action) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再" + action);
        }
        AppUser user = users.findByNickname(nickname.trim())
                .orElseThrow(() -> new ApiException(ApiErrorCode.UNAUTHORIZED, "用户不存在，请重新登录"));
        if (Boolean.TRUE.equals(user.getBlacklisted())) {
            String reason = user.getBlacklistReason() == null || user.getBlacklistReason().trim().isEmpty()
                    ? "账号已被限制"
                    : user.getBlacklistReason().trim();
            throw new ApiException(ApiErrorCode.ACCOUNT_BLOCKED, reason);
        }
        return user;
    }

    public static AppUser requireSuperAdmin(AppUserRepository users, String nickname) {
        AppUser user = requireActive(users, nickname, "使用管理后台");
        if (!ROLE_SUPER_ADMIN.equals(user.getRole())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "只有超级管理员可以访问管理后台");
        }
        return user;
    }
}
