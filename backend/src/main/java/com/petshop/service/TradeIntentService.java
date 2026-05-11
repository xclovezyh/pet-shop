package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.dto.trade.TradeIntentCreateRequest;
import com.petshop.dto.trade.TradeIntentResponse;
import com.petshop.model.MarketPost;
import com.petshop.model.TradeIntent;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.TradeIntentRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeIntentService {
    private static final String STATUS_PENDING = "待处理";
    private static final String STATUS_ACCEPTED = "已同意";
    private static final String STATUS_REJECTED = "已拒绝";
    private static final String STATUS_CANCELED = "已取消";

    private final TradeIntentRepository intents;
    private final MarketPostRepository posts;
    private final AppUserRepository users;

    public TradeIntentService(TradeIntentRepository intents, MarketPostRepository posts, AppUserRepository users) {
        this.intents = intents;
        this.posts = posts;
        this.users = users;
    }

    public List<TradeIntentResponse> list(String user, String role) {
        requireUser(user);
        List<TradeIntent> items = "owner".equals(role)
                ? intents.findByOwnerOrderByUpdatedAtDesc(user)
                : intents.findByRequesterOrderByUpdatedAtDesc(user);
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TradeIntentResponse create(TradeIntentCreateRequest request) {
        if (request == null || request.getPostId() == null) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_POST_REQUIRED);
        }
        requireUser(request.getRequester());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
        if (isBlank(post.getAuthor())) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_POST_AUTHOR_EMPTY);
        }
        if (post.getAuthor().equals(request.getRequester())) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_SELF_FORBIDDEN);
        }
        if (intents.existsByPostIdAndRequester(post.getId(), request.getRequester())) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_DUPLICATE);
        }
        String message = safe(request.getMessage()).trim();
        if (isBlank(message)) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_MESSAGE_EMPTY);
        }
        ContentSafety.validate(message);

        LocalDateTime now = LocalDateTime.now();
        TradeIntent intent = new TradeIntent();
        intent.setPostId(post.getId());
        intent.setPostTitle(post.getTitle());
        intent.setRequester(request.getRequester());
        intent.setOwner(post.getAuthor());
        intent.setMessage(message);
        intent.setStatus(STATUS_PENDING);
        intent.setCreatedAt(now);
        intent.setUpdatedAt(now);
        return toResponse(intents.save(intent));
    }

    public TradeIntentResponse updateStatus(Long id, String user, String status) {
        requireUser(user);
        TradeIntent intent = intents.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.TRADE_INTENT_NOT_FOUND));
        if (!user.equals(intent.getOwner()) && !user.equals(intent.getRequester())) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_FORBIDDEN);
        }
        if (user.equals(intent.getRequester()) && !STATUS_CANCELED.equals(status)) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_REQUESTER_CANCEL_ONLY);
        }
        if (user.equals(intent.getOwner()) && STATUS_CANCELED.equals(status)) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_OWNER_CANCEL_FORBIDDEN);
        }
        if (!STATUS_ACCEPTED.equals(status) && !STATUS_REJECTED.equals(status) && !STATUS_CANCELED.equals(status)) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_STATUS_INVALID);
        }
        intent.setStatus(status);
        intent.setUpdatedAt(LocalDateTime.now());
        return toResponse(intents.save(intent));
    }

    private TradeIntentResponse toResponse(TradeIntent intent) {
        TradeIntentResponse response = new TradeIntentResponse();
        response.setId(intent.getId());
        response.setPostId(intent.getPostId());
        response.setPostTitle(intent.getPostTitle());
        response.setRequester(intent.getRequester());
        response.setOwner(intent.getOwner());
        response.setMessage(intent.getMessage());
        response.setStatus(intent.getStatus());
        response.setCreatedAt(intent.getCreatedAt());
        response.setUpdatedAt(intent.getUpdatedAt());
        if (intent.getPostId() != null) {
            posts.findById(intent.getPostId()).ifPresent(post -> response.setPost(toPostResponse(post)));
        }
        return response;
    }

    private MarketPostResponse toPostResponse(MarketPost post) {
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

    private void requireUser(String user) {
        if (isBlank(user)) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再使用交易意向");
        }
        UserGuard.requireActive(users, user, "使用交易意向");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
