package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.favorite.FavoriteCreateRequest;
import com.petshop.dto.favorite.FavoriteResponse;
import com.petshop.model.AppUser;
import com.petshop.service.PostFavoriteService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class PostFavoriteController {
    private final PostFavoriteService favoriteService;

    public PostFavoriteController(PostFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看收藏");
        return ApiResponse.success(favoriteService.list(user.getNickname()));
    }

    @GetMapping("/post-ids")
    public ApiResponse<List<Long>> postIds(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看收藏");
        return ApiResponse.success(favoriteService.postIds(user.getNickname()));
    }

    @PostMapping
    public ApiResponse<FavoriteResponse> create(@CurrentUser AppUser currentUser,
                                                @RequestBody FavoriteCreateRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "收藏帖子");
        request.setUserNickname(user.getNickname());
        return ApiResponse.success("收藏成功", favoriteService.create(request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable Long postId, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "取消收藏");
        favoriteService.delete(postId, user.getNickname());
        return ApiResponse.success("已取消收藏", null);
    }
}
