package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.moment.MomentCommentRequest;
import com.petshop.dto.moment.MomentCommentResponse;
import com.petshop.dto.moment.MomentRequest;
import com.petshop.dto.moment.MomentResponse;
import com.petshop.service.MomentService;
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
    public ApiResponse<List<MomentResponse>> adminList(@RequestParam String admin) {
        return ApiResponse.success(momentService.adminList(admin));
    }

    @GetMapping("/{id}")
    public ApiResponse<MomentResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(momentService.detail(id));
    }

    @PostMapping
    public ApiResponse<MomentResponse> create(@RequestBody MomentRequest request) {
        return ApiResponse.success("日常发布成功", momentService.create(request));
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
                                                      @RequestBody MomentCommentRequest request) {
        return ApiResponse.success("评论成功", momentService.createComment(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam String author) {
        momentService.delete(id, author);
        return ApiResponse.success("日常已删除", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<MomentResponse> update(@PathVariable Long id,
                                              @RequestParam String author,
                                              @RequestBody MomentRequest request) {
        return ApiResponse.success("日常已更新", momentService.update(id, author, request));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<MomentResponse> audit(@PathVariable Long id,
                                             @RequestParam String admin,
                                             @RequestParam String status) {
        return ApiResponse.success("审核状态已更新", momentService.audit(id, admin, status));
    }
}
