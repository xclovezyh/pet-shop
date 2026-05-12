package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.report.ContentReportCreateRequest;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.model.AppUser;
import com.petshop.service.ContentReportService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ContentReportController {
    private final ContentReportService contentReportService;

    public ContentReportController(ContentReportService contentReportService) {
        this.contentReportService = contentReportService;
    }

    @GetMapping
    public ApiResponse<List<ContentReportResponse>> myReports(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看举报记录");
        return ApiResponse.success(contentReportService.myReports(user.getNickname()));
    }

    @GetMapping("/admin")
    public ApiResponse<List<ContentReportResponse>> adminReports(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success(contentReportService.adminReports(user.getNickname()));
    }

    @PostMapping
    public ApiResponse<ContentReportResponse> create(@CurrentUser AppUser currentUser,
                                                     @RequestBody ContentReportCreateRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "举报内容");
        request.setReporter(user.getNickname());
        return ApiResponse.success("举报已提交", contentReportService.create(request));
    }

    @PutMapping("/{id}/handle")
    public ApiResponse<ContentReportResponse> handle(@PathVariable Long id,
                                                     @CurrentUser AppUser currentUser,
                                                     @RequestBody(required = false) ContentReportHandleRequest request) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("举报已处理", contentReportService.handle(id, user.getNickname(), request));
    }
}
