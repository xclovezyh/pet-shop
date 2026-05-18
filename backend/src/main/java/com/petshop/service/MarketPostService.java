package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.post.MarketPostRequest;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.model.AppUser;
import com.petshop.model.MarketPost;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.PageSupport;
import com.petshop.support.UserGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final String STATUS_ON_SALE = "在售";
    private static final String STATUS_RESERVED = "已预约";
    private static final String STATUS_SOLD = "已成交";
    private static final String STATUS_CLOSED = "已关闭";

    private final MarketPostRepository posts;
    private final AppUserRepository users;

    public MarketPostService(MarketPostRepository posts, AppUserRepository users) {
        this.posts = posts;
        this.users = users;
    }

    public List<MarketPostResponse> list() {
        return posts.findAll().stream()
                .filter(post -> !AUDIT_REMOVED.equals(post.getAuditStatus()))
                .sorted(publicPostComparator())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<MarketPostResponse> adminList(String adminNickname, Integer page, Integer size) {
        int safePage = PageSupport.normalizePage(page);
        int safeSize = PageSupport.normalizeSize(size);
        Page<MarketPost> pageResult = posts.findAll(PageRequest.of(
                safePage - 1,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PageSupport.fromPage(pageResult, safePage, safeSize, this::toResponse);
    }

    public List<MarketPostResponse> adminList(String adminNickname) {
        return adminList(adminNickname, 1, 50).getItems();
    }

    public MarketPostResponse detail(Long id) {
        MarketPost post = findById(id);
        if (AUDIT_REMOVED.equals(post.getAuditStatus())) {
            throw new ApiException(ApiErrorCode.POST_NOT_FOUND);
        }
        return toResponse(post);
    }

    public MarketPostResponse create(AppUser currentUser, MarketPostRequest request) {
        AppUser user = requireActive(currentUser, "发布帖子");
        request.setAuthor(user.getNickname());
        validateCreateOrUpdate(user, request);
        MarketPost post = new MarketPost();
        copyFields(post, request);
        post.setAuthorUserId(user.getId());
        post.setCreatedAt(LocalDateTime.now());
        post.setContact(CONTACT_VALUE);
        post.setAuditStatus(AUDIT_APPROVED);
        if (isBlank(post.getStatus())) {
            post.setStatus(STATUS_ON_SALE);
        }
        return toResponse(posts.save(post));
    }

    public void delete(Long id, AppUser currentUser) {
        AppUser user = requireActive(currentUser, "删除帖子");
        MarketPost post = findById(id);
        if (!ownsPost(post, user)) {
            throw new ApiException(ApiErrorCode.POST_AUTHOR_MISMATCH, "只能删除自己的帖子");
        }
        posts.delete(post);
    }

    public MarketPostResponse update(Long id, AppUser currentUser, MarketPostRequest request) {
        AppUser user = requireActive(currentUser, "编辑帖子");
        MarketPost existing = findById(id);
        if (!ownsPost(existing, user)) {
            throw new ApiException(ApiErrorCode.POST_AUTHOR_MISMATCH, "只能编辑自己的帖子");
        }

        request.setAuthor(user.getNickname());
        validateCreateOrUpdate(user, request);
        existing.setTitle(request.getTitle());
        existing.setType(request.getType());
        existing.setCategory(request.getCategory());
        existing.setCity(request.getCity());
        existing.setCityCode(request.getCityCode());
        existing.setImageUrl(request.getImageUrl());
        existing.setImageUrls(request.getImageUrls());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStatus(isBlank(request.getStatus()) ? safe(existing.getStatus()) : request.getStatus());
        existing.setContact(CONTACT_VALUE);
        existing.setAuditStatus(AUDIT_APPROVED);
        if (isBlank(existing.getStatus())) {
            existing.setStatus(STATUS_ON_SALE);
        }
        return toResponse(posts.save(existing));
    }

    public MarketPostResponse audit(Long id, String adminNickname, String status) {
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_REMOVED.equals(status)) {
            throw new ApiException(ApiErrorCode.POST_AUDIT_STATUS_INVALID);
        }
        MarketPost post = findById(id);
        post.setAuditStatus(status);
        return toResponse(posts.save(post));
    }

    private void validateCreateOrUpdate(AppUser user, MarketPostRequest request) {
        if (isBlank(request.getAuthor()) || user == null || user.getId() == null) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再发布");
        }
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
        post.setCityCode(request.getCityCode());
        post.setImageUrl(request.getImageUrl());
        post.setImageUrls(request.getImageUrls());
        post.setAuthor(request.getAuthor());
        post.setStatus(request.getStatus());
        post.setPrice(request.getPrice());
        post.setDescription(request.getDescription());
    }

    private Comparator<MarketPost> publicPostComparator() {
        return Comparator
                .comparingInt(this::statusPriority)
                .thenComparing(MarketPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MarketPost::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int statusPriority(MarketPost post) {
        String status = safe(post.getStatus());
        if (STATUS_ON_SALE.equals(status)) {
            return 0;
        }
        if (STATUS_RESERVED.equals(status)) {
            return 1;
        }
        if (STATUS_SOLD.equals(status)) {
            return 2;
        }
        if (STATUS_CLOSED.equals(status)) {
            return 3;
        }
        return 4;
    }

    private MarketPostResponse toResponse(MarketPost post) {
        MarketPostResponse response = new MarketPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setType(post.getType());
        response.setCategory(post.getCategory());
        response.setCity(post.getCity());
        response.setCityCode(post.getCityCode());
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

    private AppUser requireActive(AppUser currentUser, String action) {
        return UserGuard.requireAuthenticated(currentUser, action);
    }

    private boolean ownsPost(MarketPost post, AppUser user) {
        return post.getAuthorUserId() != null && user.getId() != null && post.getAuthorUserId().equals(user.getId());
    }
}
