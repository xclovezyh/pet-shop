package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.post.MarketPostRequest;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.model.AppUser;
import com.petshop.service.MarketPostService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class MarketPostController {
    private final MarketPostService marketPostService;

    public MarketPostController(MarketPostService marketPostService) {
        this.marketPostService = marketPostService;
    }

    @GetMapping
    public ApiResponse<List<MarketPostResponse>> list() {
        return ApiResponse.success(marketPostService.list());
    }

    @GetMapping("/admin")
    public ApiResponse<List<MarketPostResponse>> adminList(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success(marketPostService.adminList(user.getNickname()));
    }

    @GetMapping("/{id}")
    public ApiResponse<MarketPostResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(marketPostService.detail(id));
    }

    @PostMapping
    public ApiResponse<MarketPostResponse> create(@CurrentUser AppUser currentUser,
                                                  @RequestBody MarketPostRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "发布帖子");
        return ApiResponse.success("帖子发布成功", marketPostService.create(user, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "删除帖子");
        marketPostService.delete(id, user);
        return ApiResponse.success("帖子已删除", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<MarketPostResponse> update(@PathVariable Long id,
                                                  @CurrentUser AppUser currentUser,
                                                  @RequestBody MarketPostRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "编辑帖子");
        return ApiResponse.success("帖子已更新", marketPostService.update(id, user, request));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<MarketPostResponse> audit(@PathVariable Long id,
                                                 @CurrentUser AppUser currentUser,
                                                 @RequestParam String status) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("审核状态已更新", marketPostService.audit(id, user.getNickname(), status));
    }
}
