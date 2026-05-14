package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.dto.trade.TradeIntentCreateRequest;
import com.petshop.dto.trade.TradeIntentResponse;
import com.petshop.model.AppUser;
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

    public List<TradeIntentResponse> list(AppUser currentUser, String role) {
        AppUser user = requireUser(currentUser);
        List<TradeIntent> items = "owner".equals(role)
                ? intents.findByOwnerUserIdOrderByUpdatedAtDesc(user.getId())
                : intents.findByRequesterUserIdOrderByUpdatedAtDesc(user.getId());
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TradeIntentResponse create(AppUser currentUser, TradeIntentCreateRequest request) {
        AppUser user = requireUser(currentUser);
        if (request == null || request.getPostId() == null) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_POST_REQUIRED);
        }
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
        if (isBlank(post.getAuthor()) || post.getAuthorUserId() == null) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_POST_AUTHOR_EMPTY);
        }
        if (post.getAuthorUserId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_SELF_FORBIDDEN);
        }
        boolean duplicate = intents.existsByPostIdAndRequesterUserId(post.getId(), user.getId());
        if (duplicate) {
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
        intent.setRequester(user.getNickname());
        intent.setRequesterUserId(user.getId());
        intent.setOwner(post.getAuthor());
        intent.setOwnerUserId(post.getAuthorUserId());
        intent.setMessage(message);
        intent.setStatus(STATUS_PENDING);
        intent.setCreatedAt(now);
        intent.setUpdatedAt(now);
        return toResponse(intents.save(intent));
    }

    public TradeIntentResponse updateStatus(Long id, AppUser currentUser, String status) {
        AppUser user = requireUser(currentUser);
        TradeIntent intent = intents.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.TRADE_INTENT_NOT_FOUND));
        boolean requester = isRequester(intent, user);
        boolean owner = isOwner(intent, user);
        if (!owner && !requester) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_FORBIDDEN);
        }
        if (requester && !STATUS_CANCELED.equals(status)) {
            throw new ApiException(ApiErrorCode.TRADE_INTENT_REQUESTER_CANCEL_ONLY);
        }
        if (owner && STATUS_CANCELED.equals(status)) {
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

    private AppUser requireUser(AppUser user) {
        if (user == null || user.getId() == null || isBlank(user.getNickname())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED, "请先登录后再使用交易意向");
        }
        return UserGuard.requireAuthenticated(user, "使用交易意向");
    }

    private boolean isRequester(TradeIntent intent, AppUser user) {
        return intent.getRequesterUserId() != null && intent.getRequesterUserId().equals(user.getId());
    }

    private boolean isOwner(TradeIntent intent, AppUser user) {
        return intent.getOwnerUserId() != null && intent.getOwnerUserId().equals(user.getId());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
