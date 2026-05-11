package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.report.ContentReportCreateRequest;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.service.ContentReportService;
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
@RequestMapping("/reports")
public class ContentReportController {
    private final ContentReportService contentReportService;

    public ContentReportController(ContentReportService contentReportService) {
        this.contentReportService = contentReportService;
    }

    @GetMapping
    public ApiResponse<List<ContentReportResponse>> myReports(@RequestParam String reporter) {
        return ApiResponse.success(contentReportService.myReports(reporter));
    }

    @GetMapping("/admin")
    public ApiResponse<List<ContentReportResponse>> adminReports(@RequestParam String admin) {
        return ApiResponse.success(contentReportService.adminReports(admin));
    }

    @PostMapping
    public ApiResponse<ContentReportResponse> create(@RequestBody ContentReportCreateRequest request) {
        return ApiResponse.success("举报已提交", contentReportService.create(request));
    }

    @PutMapping("/{id}/handle")
    public ApiResponse<ContentReportResponse> handle(@PathVariable Long id,
                                                     @RequestParam String operator,
                                                     @RequestBody(required = false) ContentReportHandleRequest request) {
        return ApiResponse.success("举报已处理", contentReportService.handle(id, operator, request));
    }
}
