package com.petshop.dto.favorite;

import com.petshop.dto.post.MarketPostResponse;

import java.time.LocalDateTime;

public class FavoriteResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private Long postId;
    private LocalDateTime createdAt;
    private MarketPostResponse post;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MarketPostResponse getPost() {
        return post;
    }

    public void setPost(MarketPostResponse post) {
        this.post = post;
    }
}
