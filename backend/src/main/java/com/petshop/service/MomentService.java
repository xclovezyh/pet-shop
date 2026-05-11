package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.moment.MomentCommentRequest;
import com.petshop.dto.moment.MomentCommentResponse;
import com.petshop.dto.moment.MomentRequest;
import com.petshop.dto.moment.MomentResponse;
import com.petshop.model.Moment;
import com.petshop.model.MomentComment;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MomentCommentRepository;
import com.petshop.repository.MomentRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MomentService {
    private static final String AUDIT_APPROVED = "审核通过";
    private static final String AUDIT_REMOVED = "已下架";

    private final MomentRepository moments;
    private final MomentCommentRepository comments;
    private final AppUserRepository users;

    public MomentService(MomentRepository moments, MomentCommentRepository comments, AppUserRepository users) {
        this.moments = moments;
        this.comments = comments;
        this.users = users;
    }

    public List<MomentResponse> list() {
        return moments.findAll().stream()
                .filter(moment -> !AUDIT_REMOVED.equals(moment.getAuditStatus()))
                .sorted(Comparator.comparing(Moment::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MomentResponse> adminList(String adminNickname) {
        UserGuard.requireSuperAdmin(users, adminNickname);
        return moments.findAll().stream()
                .sorted(Comparator.comparing(Moment::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MomentResponse detail(Long id) {
        return toResponse(findById(id));
    }

    public MomentResponse create(MomentRequest request) {
        validateCreateOrUpdate(request);
        Moment moment = new Moment();
        copyFields(moment, request);
        moment.setCreatedAt(LocalDateTime.now());
        moment.setLikes(0);
        moment.setAuditStatus(AUDIT_APPROVED);
        return toResponse(moments.save(moment));
    }

    public MomentResponse like(Long id) {
        Moment moment = findById(id);
        moment.setLikes(moment.getLikes() == null ? 1 : moment.getLikes() + 1);
        return toResponse(moments.save(moment));
    }

    public List<MomentCommentResponse> listComments(Long id) {
        findById(id);
        return comments.findByMomentIdOrderByCreatedAtAscIdAsc(id).stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    public MomentCommentResponse createComment(Long id, MomentCommentRequest request) {
        findById(id);
        if (isBlank(request.getAuthor())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再评论");
        }
        UserGuard.requireActive(users, request.getAuthor(), "评论");
        if (isBlank(request.getContent())) {
            throw new ApiException(ApiErrorCode.MOMENT_COMMENT_EMPTY);
        }
        ContentSafety.validate(request.getContent());

        MomentComment comment = new MomentComment();
        comment.setMomentId(id);
        comment.setAuthor(request.getAuthor());
        comment.setContent(request.getContent().trim());
        comment.setCreatedAt(LocalDateTime.now());
        return toCommentResponse(comments.save(comment));
    }

    public void delete(Long id, String author) {
        Moment moment = findById(id);
        if (!safe(moment.getAuthor()).equals(author)) {
            throw new ApiException(ApiErrorCode.MOMENT_AUTHOR_MISMATCH, "只能删除自己的日常");
        }
        UserGuard.requireActive(users, author, "删除日常");
        moments.delete(moment);
    }

    public MomentResponse update(Long id, String author, MomentRequest request) {
        Moment existing = findById(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ApiException(ApiErrorCode.MOMENT_AUTHOR_MISMATCH, "只能编辑自己的日常");
        }
        UserGuard.requireActive(users, author, "编辑日常");
        validateCreateOrUpdate(request);

        existing.setPetName(request.getPetName());
        existing.setCategory(request.getCategory());
        existing.setCity(request.getCity());
        existing.setImageUrl(request.getImageUrl());
        existing.setImageUrls(request.getImageUrls());
        existing.setContent(request.getContent());
        existing.setAuditStatus(AUDIT_APPROVED);
        if (existing.getLikes() == null) {
            existing.setLikes(0);
        }
        return toResponse(moments.save(existing));
    }

    public MomentResponse audit(Long id, String adminNickname, String status) {
        UserGuard.requireSuperAdmin(users, adminNickname);
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_REMOVED.equals(status)) {
            throw new ApiException(ApiErrorCode.MOMENT_AUDIT_STATUS_INVALID);
        }
        Moment moment = findById(id);
        moment.setAuditStatus(status);
        return toResponse(moments.save(moment));
    }

    private void validateCreateOrUpdate(MomentRequest request) {
        if (isBlank(request.getAuthor())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再发布");
        }
        UserGuard.requireActive(users, request.getAuthor(), "发布日常");
        if (isBlank(request.getCategory())) {
            throw new ApiException(ApiErrorCode.MOMENT_CATEGORY_REQUIRED);
        }
        String content = safe(request.getPetName()) + " " + safe(request.getContent());
        ContentSafety.validate(content);
    }

    private Moment findById(Long id) {
        return moments.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.MOMENT_NOT_FOUND));
    }

    private void copyFields(Moment moment, MomentRequest request) {
        moment.setAuthor(request.getAuthor());
        moment.setPetName(request.getPetName());
        moment.setCategory(request.getCategory());
        moment.setCity(request.getCity());
        moment.setImageUrl(request.getImageUrl());
        moment.setImageUrls(request.getImageUrls());
        moment.setContent(request.getContent());
    }

    private MomentResponse toResponse(Moment moment) {
        MomentResponse response = new MomentResponse();
        response.setId(moment.getId());
        response.setAuthor(moment.getAuthor());
        response.setPetName(moment.getPetName());
        response.setCategory(moment.getCategory());
        response.setCity(moment.getCity());
        response.setImageUrl(moment.getImageUrl());
        response.setAuditStatus(moment.getAuditStatus());
        response.setImageUrls(moment.getImageUrls());
        response.setLikes(moment.getLikes());
        response.setCreatedAt(moment.getCreatedAt());
        response.setContent(moment.getContent());
        return response;
    }

    private MomentCommentResponse toCommentResponse(MomentComment comment) {
        MomentCommentResponse response = new MomentCommentResponse();
        response.setId(comment.getId());
        response.setMomentId(comment.getMomentId());
        response.setAuthor(comment.getAuthor());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
