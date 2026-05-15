package com.petshop.service;

import com.petshop.dto.admin.AdminUserResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.model.AdminUser;
import com.petshop.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {
    @Mock
    private AdminUserRepository admins;
    @Mock
    private AdminSessionService adminSessionService;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Test
    void listShouldUseDatabasePagination() {
        AdminUser admin = new AdminUser();
        admin.setId(2L);
        admin.setUsername("admin2");
        admin.setDisplayName("admin2");
        admin.setRole("SUPER_ADMIN");
        admin.setPermissions("");
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());

        when(admins.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(admin),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")),
                2));

        PageResponse<AdminUserResponse> response = adminAuthService.list(1, 1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(2L);
        assertThat(response.getTotal()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }

    @Test
    void updateDisplayNameShouldTrimAndPersistName() {
        AdminUser admin = new AdminUser();
        admin.setId(3L);
        admin.setUsername("reviewer");
        admin.setDisplayName("reviewer");
        admin.setRole("ADMIN");
        admin.setPermissions("");
        admin.setEnabled(true);

        when(admins.findById(3L)).thenReturn(Optional.of(admin));
        when(admins.save(admin)).thenReturn(admin);

        AdminUserResponse response = adminAuthService.updateDisplayName(3L, "  审核员  ");

        assertThat(admin.getDisplayName()).isEqualTo("审核员");
        assertThat(response.getDisplayName()).isEqualTo("审核员");
    }

    @Test
    void updateDisplayNameShouldFallbackToUsernameWhenBlank() {
        AdminUser admin = new AdminUser();
        admin.setId(4L);
        admin.setUsername("operator");
        admin.setDisplayName("旧名称");
        admin.setRole("ADMIN");
        admin.setPermissions("");
        admin.setEnabled(true);

        when(admins.findById(4L)).thenReturn(Optional.of(admin));
        when(admins.save(admin)).thenReturn(admin);

        AdminUserResponse response = adminAuthService.updateDisplayName(4L, "  ");

        assertThat(admin.getDisplayName()).isEqualTo("operator");
        assertThat(response.getDisplayName()).isEqualTo("operator");
    }
}
