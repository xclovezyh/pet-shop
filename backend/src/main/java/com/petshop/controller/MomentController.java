package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.moment.MomentCommentRequest;
import com.petshop.dto.moment.MomentCommentResponse;
import com.petshop.dto.moment.MomentRequest;
import com.petshop.dto.moment.MomentResponse;
import com.petshop.model.AppUser;
import com.petshop.service.MomentService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/moments")
public class MomentController {
    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }

    @GetMapping
    public ApiResponse<List<MomentResponse>> list() {
        return ApiResponse.success(momentService.list());
    }

    @GetMapping("/admin")
    public ApiResponse<List<MomentResponse>> adminList(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success(momentService.adminList(user.getNickname()));
    }

    @GetMapping("/{id}")
    public ApiResponse<MomentResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(momentService.detail(id));
    }

    @PostMapping
    public ApiResponse<MomentResponse> create(@CurrentUser AppUser currentUser,
                                              @RequestBody MomentRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "发布日常");
        return ApiResponse.success("日常发布成功", momentService.create(user, request));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<MomentResponse> like(@PathVariable Long id) {
        return ApiResponse.success("点赞成功", momentService.like(id));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<MomentCommentResponse>> comments(@PathVariable Long id) {
        return ApiResponse.success(momentService.listComments(id));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<MomentCommentResponse> comment(@PathVariable Long id,
                                                      @CurrentUser AppUser currentUser,
                                                      @RequestBody MomentCommentRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "评论日常");
        return ApiResponse.success("评论成功", momentService.createComment(id, user, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "删除日常");
        momentService.delete(id, user);
        return ApiResponse.success("日常已删除", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<MomentResponse> update(@PathVariable Long id,
                                              @CurrentUser AppUser currentUser,
                                              @RequestBody MomentRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "编辑日常");
        return ApiResponse.success("日常已更新", momentService.update(id, user, request));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<MomentResponse> audit(@PathVariable Long id,
                                             @CurrentUser AppUser currentUser,
                                             @RequestParam String status) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("审核状态已更新", momentService.audit(id, user.getNickname(), status));
    }
}
