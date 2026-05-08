package com.petshop.controller;

import com.petshop.model.Moment;
import com.petshop.model.MomentComment;
import com.petshop.repository.MomentCommentRepository;
import com.petshop.repository.MomentRepository;
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
@RequestMapping("/moments")
public class MomentController {
    private final MomentRepository repository;
    private final MomentCommentRepository comments;

    public MomentController(MomentRepository repository, MomentCommentRepository comments) {
        this.repository = repository;
        this.comments = comments;
    }

    @GetMapping
    public List<Moment> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Moment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Moment detail(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "日常不存在"));
    }

    @PostMapping
    public Moment create(@RequestBody Moment moment) {
        validate(moment);
        moment.setCreatedAt(LocalDateTime.now());
        moment.setLikes(moment.getLikes() == null ? 0 : moment.getLikes());
        return repository.save(moment);
    }

    @PostMapping("/{id}/like")
    public Moment like(@PathVariable Long id) {
        Moment moment = detail(id);
        moment.setLikes(moment.getLikes() == null ? 1 : moment.getLikes() + 1);
        return repository.save(moment);
    }

    @GetMapping("/{id}/comments")
    public List<MomentComment> comments(@PathVariable Long id) {
        detail(id);
        return comments.findByMomentIdOrderByCreatedAtAscIdAsc(id);
    }

    @PostMapping("/{id}/comments")
    public MomentComment comment(@PathVariable Long id, @RequestBody MomentComment comment) {
        detail(id);
        if (isBlank(comment.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再评论");
        }
        if (isBlank(comment.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入评论内容");
        }
        validateText(comment.getContent());
        comment.setId(null);
        comment.setMomentId(id);
        comment.setCreatedAt(LocalDateTime.now());
        return comments.save(comment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam String author) {
        Moment moment = detail(id);
        if (!safe(moment.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能删除自己的日常");
        }
        repository.delete(moment);
    }

    @PutMapping("/{id}")
    public Moment update(@PathVariable Long id, @RequestParam String author, @RequestBody Moment update) {
        Moment existing = detail(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能编辑自己的日常");
        }
        update.setId(existing.getId());
        update.setAuthor(existing.getAuthor());
        update.setCreatedAt(existing.getCreatedAt());
        update.setLikes(existing.getLikes() == null ? 0 : existing.getLikes());
        validate(update);
        return repository.save(update);
    }

    private void validate(Moment moment) {
        if (isBlank(moment.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        if (isBlank(moment.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择宠物分类");
        }
        String content = safe(moment.getPetName()) + " " + safe(moment.getContent());
        validateText(content);
    }

    private void validateText(String content) {
        ContentSafety.validate(content);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
