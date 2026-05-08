package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.model.PostFavorite;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.PostFavoriteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorites")
public class PostFavoriteController {
    private final PostFavoriteRepository favorites;
    private final MarketPostRepository posts;

    public PostFavoriteController(PostFavoriteRepository favorites, MarketPostRepository posts) {
        this.favorites = favorites;
        this.posts = posts;
    }

    @GetMapping
    public List<PostFavorite> list(@RequestParam String user) {
        requireUser(user);
        return favorites.findByUserNicknameOrderByCreatedAtDesc(user).stream()
                .map(this::hydrate)
                .collect(Collectors.toList());
    }

    @GetMapping("/post-ids")
    public List<Long> postIds(@RequestParam String user) {
        requireUser(user);
        return favorites.findByUserNicknameOrderByCreatedAtDesc(user).stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PostFavorite create(@RequestBody FavoriteRequest request) {
        if (request == null || request.getPostId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择要收藏的帖子");
        }
        requireUser(request.getUserNickname());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));
        if (favorites.existsByUserNicknameAndPostId(request.getUserNickname(), request.getPostId())) {
            return hydrate(favorites.findFirstByUserNicknameAndPostId(request.getUserNickname(), request.getPostId()).get());
        }
        PostFavorite favorite = new PostFavorite();
        favorite.setUserNickname(request.getUserNickname());
        favorite.setPostId(post.getId());
        favorite.setCreatedAt(LocalDateTime.now());
        return hydrate(favorites.save(favorite));
    }

    @DeleteMapping("/{postId}")
    public void delete(@PathVariable Long postId, @RequestParam String user) {
        requireUser(user);
        favorites.findFirstByUserNicknameAndPostId(user, postId).ifPresent(favorites::delete);
    }

    private PostFavorite hydrate(PostFavorite favorite) {
        posts.findById(favorite.getPostId()).ifPresent(favorite::setPost);
        return favorite;
    }

    private void requireUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再收藏");
        }
    }

    public static class FavoriteRequest {
        private String userNickname;
        private Long postId;

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
    }
}
