package com.petshop.controller;

import com.petshop.model.ContentReport;
import com.petshop.model.MarketPost;
import com.petshop.model.Moment;
import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.ContentReportRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final AppUserRepository users;

    public ContentReportController(ContentReportRepository reports, MarketPostRepository posts, MomentRepository moments, AppUserRepository users) {
        this.reports = reports;
        this.posts = posts;
        this.moments = moments;
        this.users = users;
    }

    @GetMapping
    public List<ContentReport> myReports(@RequestParam String reporter) {
        requireText(reporter, "请先登录后再查看举报记录");
        return reports.findByReporterOrderByCreatedAtDesc(reporter);
    }

    @GetMapping("/admin")
    public List<ContentReport> adminReports(@RequestParam String admin) {
        UserGuard.requireSuperAdmin(users, admin);
        return reports.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ContentReport create(@RequestBody ContentReport report) {
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写举报信息");
        }
        requireText(report.getReporter(), "请先登录后再举报");
        UserGuard.requireActive(users, report.getReporter(), "举报");
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

    @PutMapping("/{id}/handle")
    public ContentReport handle(@PathVariable Long id,
                                @RequestParam String operator,
                                @RequestParam(defaultValue = "已处理") String status,
                                @RequestParam(defaultValue = "none") String action,
                                @RequestParam(defaultValue = "") String note) {
        requireText(operator, "请填写处理人");
        UserGuard.requireSuperAdmin(users, operator);
        ContentReport report = reports.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报记录不存在"));
        String author = targetAuthor(report.getTargetType(), report.getTargetId());
        if ("removeTarget".equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), "已下架");
        } else if ("restoreTarget".equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), "审核通过");
        }
        if ("blockAuthor".equals(action) || "removeAndBlockAuthor".equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), "已下架");
            if (!isBlank(author)) {
                AppUser user = users.findByNickname(author)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "内容作者不存在"));
                user.setBlacklisted(true);
                user.setBlacklistReason(isBlank(note) ? "因举报处理被限制" : note.trim());
                users.save(user);
            }
        }
        report.setStatus(status);
        report.setHandledBy(operator);
        report.setHandleNote(note);
        report.setHandledAt(LocalDateTime.now());
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

    private String targetAuthor(String targetType, Long targetId) {
        if ("post".equals(targetType)) {
            return posts.findById(targetId).map(MarketPost::getAuthor).orElse("");
        }
        if ("moment".equals(targetType)) {
            return moments.findById(targetId).map(Moment::getAuthor).orElse("");
        }
        return "";
    }

    private void setTargetAuditStatus(String targetType, Long targetId, String auditStatus) {
        if ("post".equals(targetType)) {
            MarketPost post = posts.findById(targetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的帖子不存在"));
            post.setAuditStatus(auditStatus);
            posts.save(post);
            return;
        }
        if ("moment".equals(targetType)) {
            Moment moment = moments.findById(targetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报的日常不存在"));
            moment.setAuditStatus(auditStatus);
            moments.save(moment);
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "暂不支持该内容类型处理");
    }

    private void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
