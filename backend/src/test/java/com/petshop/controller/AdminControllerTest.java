package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.admin.AdminActionLogResponse;
import com.petshop.dto.admin.AdminAuthSessionResponse;
import com.petshop.dto.admin.AdminCreateRequest;
import com.petshop.dto.admin.AdminPermissionUpdateRequest;
import com.petshop.dto.admin.AdminUserResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.dto.user.ResetPasswordRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.model.AdminUser;
import com.petshop.service.AdminActionLogService;
import com.petshop.service.AdminAuthService;
import com.petshop.service.AdminSessionService;
import com.petshop.service.ContentReportService;
import com.petshop.service.MarketPostService;
import com.petshop.service.MomentService;
import com.petshop.service.PetCategoryService;
import com.petshop.service.ReferenceDataService;
import com.petshop.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock
    private AdminAuthService adminAuthService;
    @Mock
    private AdminActionLogService adminActionLogService;
    @Mock
    private AdminSessionService adminSessionService;
    @Mock
    private UserService userService;
    @Mock
    private ContentReportService contentReportService;
    @Mock
    private MarketPostService marketPostService;
    @Mock
    private MomentService momentService;
    @Mock
    private PetCategoryService petCategoryService;
    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private AdminController controller;

    @Test
    void actionLogsShouldReturnPagedLogs() {
        AdminUser admin = superAdmin();
        PageResponse<AdminActionLogResponse> page = new PageResponse<>();
        page.setItems(Collections.emptyList());

        when(adminActionLogService.list(1, 10)).thenReturn(page);

        ApiResponse<PageResponse<AdminActionLogResponse>> response = controller.actionLogs(admin, 1, 10);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isSameAs(page);
    }

    @Test
    void resetUserPasswordShouldRecordAdminAction() {
        AdminUser admin = superAdmin();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(7L);

        when(userService.adminResetPassword(eq(7L), any(ResetPasswordRequest.class), eq("root")))
                .thenReturn(userResponse);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("salt:digest");

        ApiResponse<UserResponse> response = controller.resetUserPassword(7L, admin, request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isSameAs(userResponse);
        verify(adminActionLogService).record("root", "USER_PASSWORD_RESET", "USER", 7L, "");
    }

    private AdminUser superAdmin() {
        AdminUser admin = new AdminUser();
        admin.setUsername("root");
        admin.setRole("SUPER_ADMIN");
        admin.setEnabled(true);
        return admin;
    }
}
