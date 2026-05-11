package com.petshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminAccountService {
    private final List<String> adminUsernames;

    public AdminAccountService(@Value("${app.admin-usernames:${app.admin-nicknames:superadmin}}") String adminUsernames) {
        this.adminUsernames = Arrays.asList(adminUsernames.split(","));
    }

    public boolean isConfiguredAdmin(String username) {
        if (username == null) {
            return false;
        }
        return adminUsernames.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .anyMatch(value -> value.equals(username.trim()));
    }
}
