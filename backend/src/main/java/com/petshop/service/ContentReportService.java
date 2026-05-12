package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.report.ContentReportCreateRequest;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.model.AppUser;
import com.petshop.model.ContentReport;
import com.petshop.model.MarketPost;
import com.petshop.model.Moment;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.ContentReportRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentReportService {
    private static final String TARGET_TYPE_POST = "post";
    private static final String TARGET_TYPE_MOMENT = "moment";
    private static final String STATUS_PENDING = "待处理";
    private static final String STATUS_HANDLED = "已处理";
    private static final String ACTION_NONE = "none";
    private static final String ACTION_REMOVE_TARGET = "removeTarget";
    private static final String ACTION_RESTORE_TARGET = "restoreTarget";
    private static final String ACTION_BLOCK_AUTHOR = "blockAuthor";
    private static final String ACTION_REMOVE_AND_BLOCK_AUTHOR = "removeAndBlockAuthor";
    private static final String AUDIT_APPROVED = "审核通过";
    private static final String AUDIT_REMOVED = "已下架";

    private final ContentReportRepository reports;
    private final MarketPostRepository posts;
    private final MomentRepository moments;
    private final AppUserRepository users;

    public ContentReportService(ContentReportRepository reports,
                                MarketPostRepository posts,
                                MomentRepository moments,
                                AppUserRepository users) {
        this.reports = reports;
        this.posts = posts;
        this.moments = moments;
        this.users = users;
    }

    public List<ContentReportResponse> myReports(String reporter) {
        requireText(reporter, ApiErrorCode.REPORT_REPORTER_REQUIRED, "请先登录后再查看举报记录");
        return reports.findByReporterOrderByCreatedAtDesc(reporter.trim()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ContentReportResponse> adminReports(String admin) {
        return reports.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContentReportResponse create(ContentReportCreateRequest request) {
        if (request == null) {
            throw new ApiException(ApiErrorCode.REPORT_CREATE_PAYLOAD_REQUIRED);
        }
        requireText(request.getReporter(), ApiErrorCode.REPORT_REPORTER_REQUIRED, "请先登录后再举报");
        UserGuard.requireActive(users, request.getReporter(), "举报");
        requireText(request.getTargetType(), ApiErrorCode.REPORT_TARGET_TYPE_REQUIRED, "请选择举报内容类型");
        if (request.getTargetId() == null) {
            throw new ApiException(ApiErrorCode.REPORT_TARGET_REQUIRED);
        }
        requireText(request.getReason(), ApiErrorCode.REPORT_REASON_REQUIRED, "请填写举报原因");
        ContentSafety.validate(request.getReason());
        ensureTargetExists(request.getTargetType().trim(), request.getTargetId());

        ContentReport report = new ContentReport();
        report.setTargetType(request.getTargetType().trim());
        report.setTargetId(request.getTargetId());
        report.setReporter(request.getReporter().trim());
        report.setReason(request.getReason().trim());
        report.setStatus(STATUS_PENDING);
        report.setCreatedAt(LocalDateTime.now());
        return toResponse(reports.save(report));
    }

    public ContentReportResponse handle(Long id, String operator, ContentReportHandleRequest request) {
        requireText(operator, ApiErrorCode.REPORT_OPERATOR_REQUIRED, "请填写处理人");
        ContentReport report = reports.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.REPORT_NOT_FOUND));

        String status = request == null || isBlank(request.getStatus()) ? STATUS_HANDLED : request.getStatus().trim();
        String action = request == null || isBlank(request.getAction()) ? ACTION_NONE : request.getAction().trim();
        String note = request == null ? "" : safe(request.getNote());
        String author = targetAuthor(report.getTargetType(), report.getTargetId());

        if (ACTION_REMOVE_TARGET.equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), AUDIT_REMOVED);
        } else if (ACTION_RESTORE_TARGET.equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), AUDIT_APPROVED);
        } else if (ACTION_BLOCK_AUTHOR.equals(action) || ACTION_REMOVE_AND_BLOCK_AUTHOR.equals(action)) {
            setTargetAuditStatus(report.getTargetType(), report.getTargetId(), AUDIT_REMOVED);
            if (!isBlank(author)) {
                AppUser user = users.findByNickname(author)
                        .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "内容作者不存在"));
                user.setBlacklisted(true);
                user.setBlacklistReason(isBlank(note) ? "因举报处理被限制" : note.trim());
                users.save(user);
            }
        } else if (!ACTION_NONE.equals(action)) {
            throw new ApiException(ApiErrorCode.REPORT_ACTION_INVALID);
        }

        report.setStatus(status);
        report.setHandledBy(operator.trim());
        report.setHandleNote(note);
        report.setHandledAt(LocalDateTime.now());
        return toResponse(reports.save(report));
    }

    private void ensureTargetExists(String targetType, Long targetId) {
        if (TARGET_TYPE_POST.equals(targetType)) {
            posts.findById(targetId).orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND, "举报的帖子不存在"));
            return;
        }
        if (TARGET_TYPE_MOMENT.equals(targetType)) {
            moments.findById(targetId).orElseThrow(() -> new ApiException(ApiErrorCode.MOMENT_NOT_FOUND, "举报的动态不存在"));
            return;
        }
        throw new ApiException(ApiErrorCode.REPORT_TARGET_TYPE_UNSUPPORTED);
    }

    private String targetAuthor(String targetType, Long targetId) {
        if (TARGET_TYPE_POST.equals(targetType)) {
            return posts.findById(targetId).map(MarketPost::getAuthor).orElse("");
        }
        if (TARGET_TYPE_MOMENT.equals(targetType)) {
            return moments.findById(targetId).map(Moment::getAuthor).orElse("");
        }
        return "";
    }

    private void setTargetAuditStatus(String targetType, Long targetId, String auditStatus) {
        if (TARGET_TYPE_POST.equals(targetType)) {
            MarketPost post = posts.findById(targetId)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND, "举报的帖子不存在"));
            post.setAuditStatus(auditStatus);
            posts.save(post);
            return;
        }
        if (TARGET_TYPE_MOMENT.equals(targetType)) {
            Moment moment = moments.findById(targetId)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.MOMENT_NOT_FOUND, "举报的动态不存在"));
            moment.setAuditStatus(auditStatus);
            moments.save(moment);
            return;
        }
        throw new ApiException(ApiErrorCode.REPORT_TARGET_TYPE_UNSUPPORTED, "暂不支持该内容类型处理");
    }

    private ContentReportResponse toResponse(ContentReport report) {
        ContentReportResponse response = new ContentReportResponse();
        response.setId(report.getId());
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setReporter(report.getReporter());
        response.setStatus(report.getStatus());
        response.setHandledBy(report.getHandledBy());
        response.setCreatedAt(report.getCreatedAt());
        response.setHandledAt(report.getHandledAt());
        response.setReason(report.getReason());
        response.setHandleNote(report.getHandleNote());
        return response;
    }

    private void requireText(String value, ApiErrorCode errorCode, String message) {
        if (isBlank(value)) {
            throw new ApiException(errorCode, message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
