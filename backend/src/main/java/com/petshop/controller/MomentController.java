package com.petshop.controller;

import com.petshop.model.Moment;
import com.petshop.model.MomentComment;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MomentCommentRepository;
import com.petshop.repository.MomentRepository;
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
@RequestMapping("/moments")
public class MomentController {
    private static final String AUDIT_APPROVED = "审核通过";
    private static final String AUDIT_REMOVED = "已下架";

    private final MomentRepository repository;
    private final MomentCommentRepository comments;
    private final AppUserRepository users;

    public MomentController(MomentRepository repository, MomentCommentRepository comments, AppUserRepository users) {
        this.repository = repository;
        this.comments = comments;
        this.users = users;
    }

    @GetMapping
    public List<Moment> list() {
        return repository.findAll().stream()
                .filter(moment -> !AUDIT_REMOVED.equals(moment.getAuditStatus()))
                .sorted(Comparator.comparing(Moment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @GetMapping("/admin")
    public List<Moment> adminList(@RequestParam String admin) {
        UserGuard.requireSuperAdmin(users, admin);
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
        moment.setAuditStatus(AUDIT_APPROVED);
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
        UserGuard.requireActive(users, comment.getAuthor(), "评论");
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
        UserGuard.requireActive(users, author, "删除日常");
        repository.delete(moment);
    }

    @PutMapping("/{id}")
    public Moment update(@PathVariable Long id, @RequestParam String author, @RequestBody Moment update) {
        Moment existing = detail(id);
        if (!safe(existing.getAuthor()).equals(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能编辑自己的日常");
        }
        UserGuard.requireActive(users, author, "编辑日常");
        update.setId(existing.getId());
        update.setAuthor(existing.getAuthor());
        update.setCreatedAt(existing.getCreatedAt());
        update.setLikes(existing.getLikes() == null ? 0 : existing.getLikes());
        update.setAuditStatus(AUDIT_APPROVED);
        validate(update);
        return repository.save(update);
    }

    @PutMapping("/{id}/audit")
    public Moment audit(@PathVariable Long id, @RequestParam String admin, @RequestParam String status) {
        UserGuard.requireSuperAdmin(users, admin);
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_REMOVED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的审核状态");
        }
        Moment moment = detail(id);
        moment.setAuditStatus(status);
        return repository.save(moment);
    }

    private void validate(Moment moment) {
        if (isBlank(moment.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再发布");
        }
        UserGuard.requireActive(users, moment.getAuthor(), "发布日常");
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
