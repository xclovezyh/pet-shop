package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.favorite.FavoriteCreateRequest;
import com.petshop.dto.favorite.FavoriteResponse;
import com.petshop.service.PostFavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<List<FavoriteResponse>> list(@RequestParam String user) {
        return ApiResponse.success(favoriteService.list(user));
    }

    @GetMapping("/post-ids")
    public ApiResponse<List<Long>> postIds(@RequestParam String user) {
        return ApiResponse.success(favoriteService.postIds(user));
    }

    @PostMapping
    public ApiResponse<FavoriteResponse> create(@RequestBody FavoriteCreateRequest request) {
        return ApiResponse.success("收藏成功", favoriteService.create(request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable Long postId, @RequestParam String user) {
        favoriteService.delete(postId, user);
        return ApiResponse.success("已取消收藏", null);
    }
}
