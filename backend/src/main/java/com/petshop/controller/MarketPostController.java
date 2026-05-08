package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.repository.MarketPostRepository;
import com.petshop.support.ContentSafety;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class MarketPostController {
    private static final String CONTACT_VALUE = "站内私信";

    private final MarketPostRepository repository;

    public MarketPostController(MarketPostRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MarketPost> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(MarketPost::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MarketPost detail(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));
    }

    @PostMapping
    public MarketPost create(@RequestBody MarketPost post) {
        validate(post);
        post.setCreatedAt(LocalDateTime.now());
        post.setContact(CONTACT_VALUE);
        if (isBlank(post.getStatus())) {
            post.setStatus("在售");
        }
        return repository.save(post);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam String author) {
        MarketPost post = detail(id);
        if (!safe(post.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能删除自己的帖子");
        }
        repository.delete(post);
    }

    @PutMapping("/{id}")
    public MarketPost update(@PathVariable Long id, @RequestParam String author, @RequestBody MarketPost update) {
        MarketPost existing = detail(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能编辑自己的帖子");
        }
        update.setId(existing.getId());
        update.setAuthor(existing.getAuthor());
        update.setCreatedAt(existing.getCreatedAt());
        update.setContact(CONTACT_VALUE);
        if (isBlank(update.getStatus())) {
            update.setStatus(isBlank(existing.getStatus()) ? "在售" : existing.getStatus());
        }
        validate(update);
        return repository.save(update);
    }

    private void validate(MarketPost post) {
        if (isBlank(post.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        if (isBlank(post.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择宠物分类");
        }
        String content = safe(post.getTitle()) + " " + safe(post.getDescription()) + " " + safe(post.getContact());
        ContentSafety.validate(content);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
