package com.petshop.dto.admin;

public class AdminAuthSessionResponse {
    private String token;
    private AdminUserResponse admin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminUserResponse getAdmin() {
        return admin;
    }

    public void setAdmin(AdminUserResponse admin) {
        this.admin = admin;
    }
}
