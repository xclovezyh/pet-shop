package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.user.LoginByPasswordRequest;
import com.petshop.dto.user.LoginBySmsRequest;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.SendVerifyCodeRequest;
import com.petshop.dto.user.UpdateUserProfileRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.dto.user.VerifyCodeResponse;
import com.petshop.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(@RequestParam String admin) {
        return ApiResponse.success(userService.list(admin));
    }

    @PostMapping("/verification-code")
    public ApiResponse<VerifyCodeResponse> verificationCode(@RequestBody SendVerifyCodeRequest request) {
        return ApiResponse.success("验证码已生成", userService.sendVerifyCode(request.getPhone()));
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterUserRequest request) {
        return ApiResponse.success("注册成功", userService.register(request));
    }

    @PostMapping("/login/password")
    public ApiResponse<UserResponse> passwordLogin(@RequestBody LoginByPasswordRequest request) {
        return ApiResponse.success("登录成功", userService.loginByPassword(request));
    }

    @PostMapping("/login/sms")
    public ApiResponse<UserResponse> smsLogin(@RequestBody LoginBySmsRequest request) {
        return ApiResponse.success("登录成功", userService.loginBySms(request));
    }

    @GetMapping("/exists")
    public ApiResponse<UserResponse> exists(@RequestParam String nickname) {
        return ApiResponse.success(userService.exists(nickname));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateProfile(@PathVariable Long id, @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success("资料已更新", userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/blacklist")
    public ApiResponse<UserResponse> blacklist(@PathVariable Long id,
                                               @RequestParam String admin,
                                               @RequestParam(defaultValue = "账号存在违规行为") String reason) {
        return ApiResponse.success("账号已限制", userService.blacklist(id, admin, reason));
    }

    @PutMapping("/{id}/unblacklist")
    public ApiResponse<UserResponse> unblacklist(@PathVariable Long id, @RequestParam String admin) {
        return ApiResponse.success("账号已解除限制", userService.unblacklist(id, admin));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id,
                                                @RequestParam String admin,
                                                @RequestParam String role) {
        return ApiResponse.success("角色已更新", userService.updateRole(id, admin, role));
    }
}
