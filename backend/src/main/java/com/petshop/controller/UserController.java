package com.petshop.controller;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.api.ApiResponse;
import com.petshop.dto.user.AuthSessionResponse;
import com.petshop.dto.user.LoginByPasswordRequest;
import com.petshop.dto.user.LoginBySmsRequest;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.SendVerifyCodeRequest;
import com.petshop.dto.user.UpdateUserProfileRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.dto.user.VerifyCodeResponse;
import com.petshop.model.AppUser;
import com.petshop.service.UserService;
import com.petshop.service.UserSessionService;
import com.petshop.support.AuthenticationFilter;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
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
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserSessionService userSessionService;

    public UserController(UserService userService, UserSessionService userSessionService) {
        this.userService = userService;
        this.userSessionService = userSessionService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(@CurrentUser AppUser currentUser) {
        UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success(userService.list(currentUser.getNickname()));
    }

    @PostMapping("/verification-code")
    public ApiResponse<VerifyCodeResponse> verificationCode(@RequestBody SendVerifyCodeRequest request) {
        return ApiResponse.success("验证码已生成", userService.sendVerifyCode(request.getPhone()));
    }

    @PostMapping("/register")
    public ApiResponse<AuthSessionResponse> register(@RequestBody RegisterUserRequest request) {
        return ApiResponse.success("注册成功", userService.register(request));
    }

    @PostMapping("/login/password")
    public ApiResponse<AuthSessionResponse> passwordLogin(@RequestBody LoginByPasswordRequest request) {
        return ApiResponse.success("登录成功", userService.loginByPassword(request));
    }

    @PostMapping("/login/sms")
    public ApiResponse<AuthSessionResponse> smsLogin(@RequestBody LoginBySmsRequest request) {
        return ApiResponse.success("登录成功", userService.loginBySms(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = AuthenticationFilter.extractBearerToken(authorization);
        userSessionService.invalidate(token);
        return ApiResponse.success("已退出登录", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@CurrentUser AppUser currentUser) {
        return ApiResponse.success(userService.me(UserGuard.requireAuthenticated(currentUser, "查看账号信息")));
    }

    @GetMapping("/exists")
    public ApiResponse<UserResponse> exists(@RequestParam String nickname) {
        return ApiResponse.success(userService.exists(nickname));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateProfile(@PathVariable Long id,
                                                   @CurrentUser AppUser currentUser,
                                                   @RequestBody UpdateUserProfileRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "维护个人资料");
        if (!id.equals(user.getId())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "只能修改自己的个人资料");
        }
        return ApiResponse.success("资料已更新", userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/blacklist")
    public ApiResponse<UserResponse> blacklist(@PathVariable Long id,
                                               @CurrentUser AppUser currentUser,
                                               @RequestParam(defaultValue = "账号存在违规行为") String reason) {
        UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("账号已限制", userService.blacklist(id, currentUser.getNickname(), reason));
    }

    @PutMapping("/{id}/unblacklist")
    public ApiResponse<UserResponse> unblacklist(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("账号已解除限制", userService.unblacklist(id, currentUser.getNickname()));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id,
                                                @CurrentUser AppUser currentUser,
                                                @RequestParam String role) {
        UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("角色已更新", userService.updateRole(id, currentUser.getNickname(), role));
    }
}
