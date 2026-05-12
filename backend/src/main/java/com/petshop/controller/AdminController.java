package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.admin.AdminAuthSessionResponse;
import com.petshop.dto.admin.AdminCreateRequest;
import com.petshop.dto.admin.AdminLoginRequest;
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
import com.petshop.dto.user.UserResponse;
import com.petshop.model.AdminUser;
import com.petshop.service.AdminAuthService;
import com.petshop.service.AdminSessionService;
import com.petshop.service.ContentReportService;
import com.petshop.service.MarketPostService;
import com.petshop.service.MomentService;
import com.petshop.service.PetCategoryService;
import com.petshop.service.ReferenceDataService;
import com.petshop.service.UserService;
import com.petshop.support.AdminGuard;
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
    private final AdminSessionService adminSessionService;
    private final UserService userService;
    private final ContentReportService contentReportService;
    private final MarketPostService marketPostService;
    private final MomentService momentService;
    private final PetCategoryService petCategoryService;
    private final ReferenceDataService referenceDataService;

    public AdminController(AdminAuthService adminAuthService,
                           AdminSessionService adminSessionService,
                           UserService userService,
                           ContentReportService contentReportService,
                           MarketPostService marketPostService,
                           MomentService momentService,
                           PetCategoryService petCategoryService,
                           ReferenceDataService referenceDataService) {
        this.adminAuthService = adminAuthService;
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
                                                                      @RequestParam(defaultValue = "8") Integer size) {
        AdminGuard.requireAuthenticated(adminUser, "查看管理员账号");
        return ApiResponse.success(adminAuthService.list(page, size));
    }

    @PostMapping("/accounts")
    public ApiResponse<AdminUserResponse> createAdmin(@CurrentAdmin AdminUser adminUser,
                                                      @RequestBody AdminCreateRequest request) {
        AdminGuard.requireAuthenticated(adminUser, "创建管理员账号");
        return ApiResponse.success("管理员账号已创建", adminAuthService.create(request));
    }

    @PutMapping("/accounts/{id}/status")
    public ApiResponse<AdminUserResponse> updateAdminStatus(@PathVariable Long id,
                                                            @CurrentAdmin AdminUser adminUser,
                                                            @RequestParam boolean enabled) {
        AdminGuard.requireAuthenticated(adminUser, "维护管理员账号");
        return ApiResponse.success("管理员状态已更新", adminAuthService.updateStatus(id, enabled));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserResponse>> users(@CurrentAdmin AdminUser adminUser,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看用户列表");
        return ApiResponse.success(userService.list(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/users/{id}/blacklist")
    public ApiResponse<UserResponse> blacklist(@PathVariable Long id,
                                               @CurrentAdmin AdminUser adminUser,
                                               @RequestParam(defaultValue = "账号存在违规行为") String reason) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "限制用户账号");
        return ApiResponse.success("用户账号已限制", userService.blacklist(id, currentAdmin.getUsername(), reason));
    }

    @PutMapping("/users/{id}/unblacklist")
    public ApiResponse<UserResponse> unblacklist(@PathVariable Long id, @CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "解除用户限制");
        return ApiResponse.success("用户账号已解除限制", userService.unblacklist(id, currentAdmin.getUsername()));
    }

    @GetMapping("/reports")
    public ApiResponse<PageResponse<ContentReportResponse>> reports(@CurrentAdmin AdminUser adminUser,
                                                                    @RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "8") Integer size) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看举报记录");
        return ApiResponse.success(contentReportService.adminReports(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/reports/{id}/handle")
    public ApiResponse<ContentReportResponse> handleReport(@PathVariable Long id,
                                                           @CurrentAdmin AdminUser adminUser,
                                                           @RequestBody(required = false) ContentReportHandleRequest request) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "处理举报");
        return ApiResponse.success("举报已处理", contentReportService.handle(id, currentAdmin.getUsername(), request));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<MarketPostResponse>> posts(@CurrentAdmin AdminUser adminUser,
                                                               @RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "8") Integer size) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看帖子审核列表");
        return ApiResponse.success(marketPostService.adminList(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/posts/{id}/audit")
    public ApiResponse<MarketPostResponse> auditPost(@PathVariable Long id,
                                                     @CurrentAdmin AdminUser adminUser,
                                                     @RequestParam String status) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "审核帖子");
        return ApiResponse.success("帖子审核状态已更新", marketPostService.audit(id, currentAdmin.getUsername(), status));
    }

    @GetMapping("/moments")
    public ApiResponse<PageResponse<MomentResponse>> moments(@CurrentAdmin AdminUser adminUser,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "8") Integer size) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看日常审核列表");
        return ApiResponse.success(momentService.adminList(currentAdmin.getUsername(), page, size));
    }

    @PutMapping("/moments/{id}/audit")
    public ApiResponse<MomentResponse> auditMoment(@PathVariable Long id,
                                                   @CurrentAdmin AdminUser adminUser,
                                                   @RequestParam String status) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "审核日常内容");
        return ApiResponse.success("日常审核状态已更新", momentService.audit(id, currentAdmin.getUsername(), status));
    }

    @GetMapping("/categories")
    public ApiResponse<PageResponse<PetCategoryResponse>> categories(@CurrentAdmin AdminUser adminUser,
                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
        AdminGuard.requireAuthenticated(adminUser, "查看分类配置");
        return ApiResponse.success(petCategoryService.adminList(page, size));
    }

    @PostMapping("/categories")
    public ApiResponse<PetCategoryResponse> createCategory(@CurrentAdmin AdminUser adminUser,
                                                           @RequestBody PetCategoryRequest request) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "创建分类");
        return ApiResponse.success("分类已创建", petCategoryService.create(currentAdmin.getUsername(), request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<PetCategoryResponse> updateCategory(@PathVariable Long id,
                                                           @CurrentAdmin AdminUser adminUser,
                                                           @RequestBody PetCategoryRequest request) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "更新分类");
        return ApiResponse.success("分类已更新", petCategoryService.update(id, currentAdmin.getUsername(), request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id, @CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "删除分类");
        petCategoryService.delete(id, currentAdmin.getUsername());
        return ApiResponse.success("分类已删除", null);
    }

    @GetMapping("/regions")
    public ApiResponse<PageResponse<RegionAreaResponse>> regions(@CurrentAdmin AdminUser adminUser,
                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看地区库");
        return ApiResponse.success(referenceDataService.regionAdminList(currentAdmin.getUsername(), page, size));
    }

    @GetMapping("/regions/tree")
    public ApiResponse<List<AdminRegionProvinceResponse>> regionTree(@CurrentAdmin AdminUser adminUser) {
        AdminUser currentAdmin = AdminGuard.requireAuthenticated(adminUser, "查看地区库层级");
        return ApiResponse.success(referenceDataService.regionAdminTree(currentAdmin.getUsername()));
    }
}
