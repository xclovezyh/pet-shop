package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
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
    private static final String AUDIT_APPROVED = "审核通过";
    private static final String AUDIT_REMOVED = "已下架";

    private final MarketPostRepository repository;
    private final AppUserRepository users;

    public MarketPostController(MarketPostRepository repository, AppUserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @GetMapping
    public List<MarketPost> list() {
        return repository.findAll().stream()
                .filter(post -> !AUDIT_REMOVED.equals(post.getAuditStatus()))
                .sorted(Comparator.comparing(MarketPost::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/admin")
    public List<MarketPost> adminList() {
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
        post.setAuditStatus(AUDIT_APPROVED);
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
        UserGuard.requireActive(users, author, "删除帖子");
        repository.delete(post);
    }

    @PutMapping("/{id}")
    public MarketPost update(@PathVariable Long id, @RequestParam String author, @RequestBody MarketPost update) {
        MarketPost existing = detail(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能编辑自己的帖子");
        }
        UserGuard.requireActive(users, author, "编辑帖子");
        update.setId(existing.getId());
        update.setAuthor(existing.getAuthor());
        update.setCreatedAt(existing.getCreatedAt());
        update.setContact(CONTACT_VALUE);
        update.setAuditStatus(AUDIT_APPROVED);
        if (isBlank(update.getStatus())) {
            update.setStatus(isBlank(existing.getStatus()) ? "在售" : existing.getStatus());
        }
        validate(update);
        return repository.save(update);
    }

    @PutMapping("/{id}/audit")
    public MarketPost audit(@PathVariable Long id, @RequestParam String status) {
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_REMOVED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的审核状态");
        }
        MarketPost post = detail(id);
        post.setAuditStatus(status);
        return repository.save(post);
    }

    private void validate(MarketPost post) {
        if (isBlank(post.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        UserGuard.requireActive(users, post.getAuthor(), "发布帖子");
        if (isBlank(post.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择宠物分类");
        }
        if (post.getPrice() != null && post.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "价格不能小于 0");
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
