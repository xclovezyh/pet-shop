package com.petshop.controller;

import com.petshop.model.Moment;
import com.petshop.repository.MomentRepository;
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
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/moments")
public class MomentController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");

    private final MomentRepository repository;

    public MomentController(MomentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Moment> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Moment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @PostMapping
    public Moment create(@RequestBody Moment moment) {
        validate(moment);
        moment.setCreatedAt(LocalDateTime.now());
        moment.setLikes(moment.getLikes() == null ? 0 : moment.getLikes());
        return repository.save(moment);
    }

    private void validate(Moment moment) {
        if (isBlank(moment.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        if (isBlank(moment.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择宠物分类");
        }
        String content = safe(moment.getPetName()) + " " + safe(moment.getContent());
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
