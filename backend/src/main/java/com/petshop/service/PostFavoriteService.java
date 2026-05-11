package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.favorite.FavoriteCreateRequest;
import com.petshop.dto.favorite.FavoriteResponse;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.model.MarketPost;
import com.petshop.model.PostFavorite;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.PostFavoriteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostFavoriteService {
    private final PostFavoriteRepository favorites;
    private final MarketPostRepository posts;

    public PostFavoriteService(PostFavoriteRepository favorites, MarketPostRepository posts) {
        this.favorites = favorites;
        this.posts = posts;
    }

    public List<FavoriteResponse> list(String userNickname) {
        requireUser(userNickname);
        return favorites.findByUserNicknameOrderByCreatedAtDesc(userNickname).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Long> postIds(String userNickname) {
        requireUser(userNickname);
        return favorites.findByUserNicknameOrderByCreatedAtDesc(userNickname).stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());
    }

    public FavoriteResponse create(FavoriteCreateRequest request) {
        if (request == null || request.getPostId() == null) {
            throw new ApiException(ApiErrorCode.FAVORITE_POST_REQUIRED);
        }
        requireUser(request.getUserNickname());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
        if (favorites.existsByUserNicknameAndPostId(request.getUserNickname(), request.getPostId())) {
            return toResponse(favorites.findFirstByUserNicknameAndPostId(request.getUserNickname(), request.getPostId()).get());
        }
        PostFavorite favorite = new PostFavorite();
        favorite.setUserNickname(request.getUserNickname());
        favorite.setPostId(post.getId());
        favorite.setCreatedAt(LocalDateTime.now());
        return toResponse(favorites.save(favorite));
    }

    public void delete(Long postId, String userNickname) {
        requireUser(userNickname);
        favorites.findFirstByUserNicknameAndPostId(userNickname, postId).ifPresent(favorites::delete);
    }

    private FavoriteResponse toResponse(PostFavorite favorite) {
        FavoriteResponse response = new FavoriteResponse();
        response.setId(favorite.getId());
        response.setUserNickname(favorite.getUserNickname());
        response.setPostId(favorite.getPostId());
        response.setCreatedAt(favorite.getCreatedAt());
        posts.findById(favorite.getPostId()).ifPresent(post -> response.setPost(toPostResponse(post)));
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

    private void requireUser(String userNickname) {
        if (userNickname == null || userNickname.trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.FAVORITE_USER_REQUIRED);
        }
    }
}
