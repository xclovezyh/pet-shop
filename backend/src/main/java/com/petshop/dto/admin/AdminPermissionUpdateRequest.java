package com.petshop.dto.admin;

import java.util.List;

public class AdminPermissionUpdateRequest {
    private List<String> permissions;

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
