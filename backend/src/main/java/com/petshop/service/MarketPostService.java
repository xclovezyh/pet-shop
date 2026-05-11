package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.post.MarketPostRequest;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.model.MarketPost;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketPostService {
    private static final String CONTACT_VALUE = "站内私信";
    private static final String AUDIT_APPROVED = "审核通过";
    private static final String AUDIT_REMOVED = "已下架";

    private final MarketPostRepository posts;
    private final AppUserRepository users;

    public MarketPostService(MarketPostRepository posts, AppUserRepository users) {
        this.posts = posts;
        this.users = users;
    }

    public List<MarketPostResponse> list() {
        return posts.findAll().stream()
                .filter(post -> !AUDIT_REMOVED.equals(post.getAuditStatus()))
                .sorted(Comparator.comparing(MarketPost::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MarketPostResponse> adminList(String adminNickname) {
        UserGuard.requireSuperAdmin(users, adminNickname);
        return posts.findAll().stream()
                .sorted(Comparator.comparing(MarketPost::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MarketPostResponse detail(Long id) {
        return toResponse(findById(id));
    }

    public MarketPostResponse create(MarketPostRequest request) {
        validateCreateOrUpdate(request);
        MarketPost post = new MarketPost();
        copyFields(post, request);
        post.setCreatedAt(LocalDateTime.now());
        post.setContact(CONTACT_VALUE);
        post.setAuditStatus(AUDIT_APPROVED);
        if (isBlank(post.getStatus())) {
            post.setStatus("在售");
        }
        return toResponse(posts.save(post));
    }

    public void delete(Long id, String author) {
        MarketPost post = findById(id);
        if (!safe(post.getAuthor()).equals(author)) {
            throw new ApiException(ApiErrorCode.POST_AUTHOR_MISMATCH, "只能删除自己的帖子");
        }
        UserGuard.requireActive(users, author, "删除帖子");
        posts.delete(post);
    }

    public MarketPostResponse update(Long id, String author, MarketPostRequest request) {
        MarketPost existing = findById(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ApiException(ApiErrorCode.POST_AUTHOR_MISMATCH, "只能编辑自己的帖子");
        }
        UserGuard.requireActive(users, author, "编辑帖子");

        validateCreateOrUpdate(request);
        existing.setTitle(request.getTitle());
        existing.setType(request.getType());
        existing.setCategory(request.getCategory());
        existing.setCity(request.getCity());
        existing.setImageUrl(request.getImageUrl());
        existing.setImageUrls(request.getImageUrls());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStatus(isBlank(request.getStatus()) ? safe(existing.getStatus()) : request.getStatus());
        existing.setContact(CONTACT_VALUE);
        existing.setAuditStatus(AUDIT_APPROVED);
        if (isBlank(existing.getStatus())) {
            existing.setStatus("在售");
        }
        return toResponse(posts.save(existing));
    }

    public MarketPostResponse audit(Long id, String adminNickname, String status) {
        UserGuard.requireSuperAdmin(users, adminNickname);
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_REMOVED.equals(status)) {
            throw new ApiException(ApiErrorCode.POST_AUDIT_STATUS_INVALID);
        }
        MarketPost post = findById(id);
        post.setAuditStatus(status);
        return toResponse(posts.save(post));
    }

    private void validateCreateOrUpdate(MarketPostRequest request) {
        if (isBlank(request.getAuthor())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再发布");
        }
        UserGuard.requireActive(users, request.getAuthor(), "发布帖子");
        if (isBlank(request.getCategory())) {
            throw new ApiException(ApiErrorCode.POST_CATEGORY_REQUIRED);
        }
        if (request.getPrice() != null && request.getPrice().signum() < 0) {
            throw new ApiException(ApiErrorCode.POST_PRICE_INVALID);
        }
        String content = safe(request.getTitle()) + " " + safe(request.getDescription()) + " " + CONTACT_VALUE;
        ContentSafety.validate(content);
    }

    private MarketPost findById(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
    }

    private void copyFields(MarketPost post, MarketPostRequest request) {
        post.setTitle(request.getTitle());
        post.setType(request.getType());
        post.setCategory(request.getCategory());
        post.setCity(request.getCity());
        post.setImageUrl(request.getImageUrl());
        post.setImageUrls(request.getImageUrls());
        post.setAuthor(request.getAuthor());
        post.setStatus(request.getStatus());
        post.setPrice(request.getPrice());
        post.setDescription(request.getDescription());
    }

    private MarketPostResponse toResponse(MarketPost post) {
        MarketPostResponse response = new MarketPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setType(post.getType());
        response.setCategory(post.getCategory());
        response.setCity(post.getCity());
        response.setImageUrl(post.getImageUrl());
        response.setImageUrls(post.getImageUrls());
        response.setContact(post.getContact());
        response.setAuthor(post.getAuthor());
        response.setStatus(post.getStatus());
        response.setAuditStatus(post.getAuditStatus());
        response.setPrice(post.getPrice());
        response.setCreatedAt(post.getCreatedAt());
        response.setDescription(post.getDescription());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
