package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.repository.MarketPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class MarketPostController {
    private static final String CONTACT_VALUE = "站内私信";
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");

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

    @PostMapping
    public MarketPost create(@RequestBody MarketPost post) {
        validate(post);
        post.setCreatedAt(LocalDateTime.now());
        post.setContact(CONTACT_VALUE);
        return repository.save(post);
    }

    private void validate(MarketPost post) {
        if (isBlank(post.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        if (isBlank(post.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择宠物分类");
        }
        String content = safe(post.getTitle()) + " " + safe(post.getDescription()) + " " + safe(post.getContact());
        if (PHONE_PATTERN.matcher(content).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "禁止填写手机号，请使用站内私信");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
