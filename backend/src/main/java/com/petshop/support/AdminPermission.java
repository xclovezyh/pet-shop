package com.petshop.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum AdminPermission {
    USER_MODERATE("USER_MODERATE", "用户治理", "查看用户列表、限制与解除限制"),
    REPORT_REVIEW("REPORT_REVIEW", "举报处理", "查看举报并执行处理动作"),
    POST_AUDIT("POST_AUDIT", "帖子审核", "审核、恢复和下架交易帖"),
    MOMENT_AUDIT("MOMENT_AUDIT", "动态审核", "审核、恢复和下架社区动态"),
    CATEGORY_MANAGE("CATEGORY_MANAGE", "分类管理", "维护主站宠物分类"),
    REGION_VIEW("REGION_VIEW", "地区库查看", "查看全国省市区标准地区库");

    private final String code;
    private final String displayName;
    private final String description;

    AdminPermission(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static AdminPermission fromCode(String code) {
        for (AdminPermission permission : values()) {
            if (permission.code.equalsIgnoreCase(code == null ? "" : code.trim())) {
                return permission;
            }
        }
        return null;
    }

    public static LinkedHashSet<AdminPermission> parseCodes(List<String> codes) {
        LinkedHashSet<AdminPermission> permissions = new LinkedHashSet<>();
        if (codes == null) {
            return permissions;
        }
        for (String code : codes) {
            AdminPermission permission = fromCode(code);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public static LinkedHashSet<AdminPermission> parseCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new LinkedHashSet<>();
        }
        return parseCodes(Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList()));
    }

    public static String toCsv(Set<AdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return permissions.stream().map(AdminPermission::getCode).collect(Collectors.joining(","));
    }

    public static List<String> toCodeList(Set<AdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissions.stream().map(AdminPermission::getCode).collect(Collectors.toList());
    }
}
