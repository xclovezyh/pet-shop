package com.petshop.controller;

import com.petshop.model.ContentReport;
import com.petshop.repository.ContentReportRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.support.ContentSafety;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ContentReportController {
    private final ContentReportRepository reports;
    private final MarketPostRepository posts;
    private final MomentRepository moments;

    public ContentReportController(ContentReportRepository reports, MarketPostRepository posts, MomentRepository moments) {
        this.reports = reports;
        this.posts = posts;
        this.moments = moments;
    }

    @GetMapping
    public List<ContentReport> myReports(@RequestParam String reporter) {
        requireText(reporter, "请先登录后再查看举报记录");
        return reports.findByReporterOrderByCreatedAtDesc(reporter);
    }

    @PostMapping
    public ContentReport create(@RequestBody ContentReport report) {
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写举报信息");
        }
        requireText(report.getReporter(), "请先登录后再举报");
        requireText(report.getTargetType(), "请选择举报内容类型");
        if (report.getTargetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择举报内容");
        }
        requireText(report.getReason(), "请填写举报原因");
        ContentSafety.validate(report.getReason());
        ensureTargetExists(report.getTargetType(), report.getTargetId());
        report.setId(null);
        report.setStatus("待处理");
        report.setCreatedAt(LocalDateTime.now());
        return reports.save(report);
    }

    private void ensureTargetExists(String targetType, Long targetId) {
        if ("post".equals(targetType)) {
            posts.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的帖子不存在"));
            return;
        }
        if ("moment".equals(targetType)) {
            moments.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的日常不存在"));
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "暂不支持该内容类型举报");
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
