package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.admin.AdminAuthSessionResponse;
import com.petshop.dto.admin.AdminActionLogResponse;
import com.petshop.dto.admin.AdminCreateRequest;
import com.petshop.dto.admin.AdminDisplayNameUpdateRequest;
import com.petshop.dto.admin.AdminLoginRequest;
import com.petshop.dto.admin.AdminPermissionOptionResponse;
import com.petshop.dto.admin.AdminPermissionUpdateRequest;
import com.petshop.dto.admin.AdminUserResponse;
import com.petshop.dto.category.PetCategoryRequest;
import com.petshop.dto.category.PetCategoryResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.moment.MomentResponse;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.dto.reference.AdminRegionProvinceResponse;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.dto.user.AdminDisableUserRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.dto.user.ResetPasswordRequest;
import com.petshop.model.AdminUser;
import com.petshop.service.AdminAuthService;
import com.petshop.service.AdminActionLogService;
import com.petshop.service.AdminSessionService;
import com.petshop.service.ContentReportService;
import com.petshop.service.MarketPostService;
import com.petshop.service.MomentService;
import com.petshop.service.PetCategoryService;
import com.petshop.service.ReferenceDataService;
import com.petshop.service.UserService;
import com.petshop.support.AdminGuard;
import com.petshop.support.AdminPermission;
import com.petshop.support.AuthenticationFilter;
import com.petshop.support.CurrentAdmin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminAuthService adminAuthService;
    private final AdminActionLogService adminActionLogService;
    private final AdminSessionService adminSessionService;
    private final UserService userService;
    private final ContentReportService contentReportService;
    private final MarketPostService marketPostService;
    private final MomentService momentService;
    private final PetCategoryService petCategoryService;
    private final ReferenceDataService referenceDataService;

    public AdminController(AdminAuthService adminAuthService,
                           AdminActionLogService adminActionLogService,
                           AdminSessionService adminSessionService,
                           UserService userService,
                           ContentReportService contentReportService,
                           MarketPostService marketPostService,
                           MomentService momentService,
                           PetCategoryService petCategoryService,
                           ReferenceDataService referenceDataService) {
        this.adminAuthService = adminAuthService;
        this.adminActionLogService = adminActionLogService;
        this.adminSessionService = adminSessionService;
        this.userService = userService;
        this.contentReportService = contentReportService;
        this.marketPostService = marketPostService;
        this.momentService = momentService;
        this.petCategoryService = petCategoryService;
        this.referenceDataService = referenceDataService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<AdminAuthSessionResponse> login(@RequestBody AdminLoginRequest request) {
        return ApiResponse.success("管理员登录成功", adminAuthService.login(request));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        adminSessionService.invalidate(AuthenticationFilter.extractBearerToken(authorization));
        return ApiResponse.success("已退出管理员登录", null);
    }

    @GetMapping("/auth/me")
    public ApiResponse<AdminUserResponse> me(@CurrentAdmin AdminUser adminUser) {
        return ApiResponse.success(adminAuthService.me(AdminGuard.requireAuthenticated(adminUser, "查看管理员信息")));
    }

    @GetMapping("/accounts")
    public ApiResponse<PageResponse<AdminUserResponse>> adminAccounts(@CurrentAdmin AdminUser adminUser,
                                                                      @RequestParam(defaultValue = "1") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        AdminGuard.requireSuperAdmin(adminUser, "查看管理员账号");
        return ApiResponse.success(adminAuthService.list(page, size));
    }

    @GetMapping("/accounts/permission-options")
    public ApiResponse<List<AdminPermissionOptionResponse>> permissionOptions(@CurrentAdmin AdminUser adminUser) {
        AdminGuard.requireSuperAdmin(adminUser, "查看管理员权限模板");
        return ApiResponse.success(adminAuthService.permissionOptions());
    }

    @PostMapping("/accounts")
    public ApiResponse<AdminUserResponse> createAdmin(@CurrentAdmin AdminUser adminUser,
                                                      @RequestBody AdminCreateRequest request) {
        AdminGuard.requireSuperAdmin(adminUser, "创建管理员账号");
        return ApiResponse.success("管理员账号已创建", adminAuthService.create(request));
    }

    @PutMapping("/accounts/{id}/status")
    public ApiResponse<AdminUserResponse> updateAdminStatus(@PathVariable Long id,
                                                            @CurrentAdmin AdminUser adminUser,
                                                            @RequestParam boolean enabled) {
        AdminUser currentAdmin = AdminGuard.requireSuperAdmin(adminUser, "维护管理员账号");
        AdminUserResponse response = adminAuthService.updateStatus(id, enabled, currentAdmin);
        recordAdminAction(currentAdmin, "ADMIN_STATUS_UPDATE", "ADMIN", id, "enabled=" + enabled);
        return ApiResponse.success("管理员状态已更新", response);
    }

    @PutMapping("/accounts/{id}/display-name")
    public ApiResponse<AdminUserResponse> updateAdminDisplayName(@PathVariable Long id,
                                                                 @CurrentAdmin AdminUser adminUser,
                                                                 @RequestBody(required = false) AdminDisplayNameUpdateRequest request) {
        AdminUser currentAdmin = AdminGuard.requireSuperAdmin(adminUser, "修改管理员显示名");
        String displayName = request == null ? "" : request.getDisplayName();
        AdminUserResponse response = adminAuthService.updateDisplayName(id, displayName);
        recordAdminAction(currentAdmin, "ADMIN_DISPLAY_NAME_UPDATE", "ADMIN", id, response.getDisplayName());
        return ApiResponse.success("管理员显示名已更新", response);
    }

    @PutMapping("/accounts/{id}/permissions")
    public ApiResponse<AdminUserResponse> updateAdminPermissions(@PathVariable Long id,
                                                                 @CurrentAdmin AdminUser adminUser,
                                                                 @RequestBody AdminPermissionUpdateRequest request) {
        AdminUser currentAdmin = AdminGuard.requireSuperAdmin(adminUser, "分配管理员权限");
        AdminUserResponse response = adminAuthService.updatePermissions(id, request == null ? null : request.getPermissions(), currentAdmin);
        recordAdminAction(currentAdmin, "ADMIN_PERMISSION_UPDATE", "ADMIN", id, request == null ? "" : String.valueOf(request.getPermissions()));
        return ApiResponse.success("管理员权限已更新", response);
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserResponse>> users(@CurrentAdmin AdminUser adminUser,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.USER_MODERATE, "查看用户列表");
        return ApiResponse.success(userService.list(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/users/{id}/blacklist")
    public ApiResponse<UserResponse> blacklist(@PathVariable Long id,
                                               @CurrentAdmin AdminUser adminUser,
                                               @RequestBody(required = false) AdminDisableUserRequest request) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.USER_MODERATE, "限制用户账号");
        String reason = request == null ? "" : request.getReason();
        UserResponse response = userService.blacklist(id, currentAdmin.getUsername(), reason);
        recordAdminAction(currentAdmin, "USER_BLACKLIST", "USER", id, reason);
        return ApiResponse.success("用户账号已限制", response);
    }

    @PutMapping("/users/{id}/unblacklist")
    public ApiResponse<UserResponse> unblacklist(@PathVariable Long id, @CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.USER_MODERATE, "解除用户限制");
        UserResponse response = userService.unblacklist(id, currentAdmin.getUsername());
        recordAdminAction(currentAdmin, "USER_UNBLACKLIST", "USER", id, "");
        return ApiResponse.success("用户账号已解除限制", response);
    }

    @PutMapping("/users/{id}/password")
    public ApiResponse<UserResponse> resetUserPassword(@PathVariable Long id,
                                                       @CurrentAdmin AdminUser adminUser,
                                                       @RequestBody ResetPasswordRequest request) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.USER_MODERATE, "重置用户密码");
        UserResponse response = userService.adminResetPassword(id, request, currentAdmin.getUsername());
        recordAdminAction(currentAdmin, "USER_PASSWORD_RESET", "USER", id, "");
        return ApiResponse.success("用户密码已重置", response);
    }

    @GetMapping("/reports")
    public ApiResponse<PageResponse<ContentReportResponse>> reports(@CurrentAdmin AdminUser adminUser,
                                                                    @RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.REPORT_REVIEW, "查看举报记录");
        return ApiResponse.success(contentReportService.adminReports(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/reports/{id}/handle")
    public ApiResponse<ContentReportResponse> handleReport(@PathVariable Long id,
                                                           @CurrentAdmin AdminUser adminUser,
                                                           @RequestBody(required = false) ContentReportHandleRequest request) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.REPORT_REVIEW, "处理举报");
        ContentReportResponse response = contentReportService.handle(id, currentAdmin.getUsername(), request);
        recordAdminAction(currentAdmin, "REPORT_HANDLE", "REPORT", id, request == null ? "" : String.valueOf(request.getAction()));
        return ApiResponse.success("举报已处理", response);
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<MarketPostResponse>> posts(@CurrentAdmin AdminUser adminUser,
                                                               @RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.POST_AUDIT, "查看帖子审核列表");
        return ApiResponse.success(marketPostService.adminList(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/posts/{id}/audit")
    public ApiResponse<MarketPostResponse> auditPost(@PathVariable Long id,
                                                     @CurrentAdmin AdminUser adminUser,
                                                     @RequestParam String status) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.POST_AUDIT, "审核帖子");
        MarketPostResponse response = marketPostService.audit(id, currentAdmin.getUsername(), status);
        recordAdminAction(currentAdmin, "POST_AUDIT", "POST", id, status);
        return ApiResponse.success("帖子审核状态已更新", response);
    }

    @GetMapping("/moments")
    public ApiResponse<PageResponse<MomentResponse>> moments(@CurrentAdmin AdminUser adminUser,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.MOMENT_AUDIT, "查看动态审核列表");
        return ApiResponse.success(momentService.adminList(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/moments/{id}/audit")
    public ApiResponse<MomentResponse> auditMoment(@PathVariable Long id,
                                                   @CurrentAdmin AdminUser adminUser,
                                                   @RequestParam String status) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.MOMENT_AUDIT, "审核动态内容");
        MomentResponse response = momentService.audit(id, currentAdmin.getUsername(), status);
        recordAdminAction(currentAdmin, "MOMENT_AUDIT", "MOMENT", id, status);
        return ApiResponse.success("动态审核状态已更新", response);
    }

    @GetMapping("/categories")
    public ApiResponse<PageResponse<PetCategoryResponse>> categories(@CurrentAdmin AdminUser adminUser,
                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
        AdminGuard.requirePermission(adminUser, AdminPermission.CATEGORY_MANAGE, "查看分类配置");
        return ApiResponse.success(petCategoryService.adminList(page, size));
    }

    @PostMapping("/categories")
    public ApiResponse<PetCategoryResponse> createCategory(@CurrentAdmin AdminUser adminUser,
                                                           @RequestBody PetCategoryRequest request) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.CATEGORY_MANAGE, "创建分类");
        PetCategoryResponse response = petCategoryService.create(currentAdmin.getUsername(), request);
        recordAdminAction(currentAdmin, "CATEGORY_CREATE", "CATEGORY", response.getId(), request == null ? "" : request.getName());
        return ApiResponse.success("分类已创建", response);
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<PetCategoryResponse> updateCategory(@PathVariable Long id,
                                                           @CurrentAdmin AdminUser adminUser,
                                                           @RequestBody PetCategoryRequest request) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.CATEGORY_MANAGE, "更新分类");
        PetCategoryResponse response = petCategoryService.update(id, currentAdmin.getUsername(), request);
        recordAdminAction(currentAdmin, "CATEGORY_UPDATE", "CATEGORY", id, request == null ? "" : request.getName());
        return ApiResponse.success("分类已更新", response);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id, @CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.CATEGORY_MANAGE, "删除分类");
        petCategoryService.delete(id, currentAdmin.getUsername());
        recordAdminAction(currentAdmin, "CATEGORY_DELETE", "CATEGORY", id, "");
        return ApiResponse.success("分类已删除", null);
    }

    @GetMapping("/regions")
    public ApiResponse<PageResponse<RegionAreaResponse>> regions(@CurrentAdmin AdminUser adminUser,
                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.REGION_VIEW, "查看地区库");
        return ApiResponse.success(referenceDataService.regionAdminList(currentAdmin.getUsername(), page, size));
    }

    @GetMapping("/action-logs")
    public ApiResponse<PageResponse<AdminActionLogResponse>> actionLogs(@CurrentAdmin AdminUser adminUser,
                                                                        @RequestParam(defaultValue = "1") Integer page,
                                                                        @RequestParam(defaultValue = "10") Integer size) {
        AdminGuard.requireSuperAdmin(adminUser, "查看审计日志");
        return ApiResponse.success(adminActionLogService.list(page, size));
    }

    @GetMapping("/regions/tree")
    public ApiResponse<List<AdminRegionProvinceResponse>> regionTree(@CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requirePermission(adminUser, AdminPermission.REGION_VIEW, "查看地区库层级");
        return ApiResponse.success(referenceDataService.regionAdminTree(currentAdmin.getUsername()));
    }

    private void recordAdminAction(AdminUser adminUser, String action, String targetType, Long targetId, String detail) {
        adminActionLogService.record(adminUser.getUsername(), action, targetType, targetId, detail);
    }
}
