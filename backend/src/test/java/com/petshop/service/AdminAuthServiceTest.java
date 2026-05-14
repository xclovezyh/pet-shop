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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {
    @Mock
    private AdminUserRepository admins;

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
}
