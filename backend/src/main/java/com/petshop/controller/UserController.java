package com.petshop.controller;

import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");

    private final AppUserRepository repository;

    public UserController(AppUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AppUser> list() {
        return repository.findAll();
    }

    @PostMapping("/login")
    public AppUser login(@RequestBody Map<String, String> payload) {
        String nickname = payload.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入昵称");
        }
        nickname = nickname.trim();
        if (PHONE_PATTERN.matcher(nickname).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "昵称不能使用手机号");
        }
        final String finalNickname = nickname;
        return repository.findByNickname(nickname).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setNickname(finalNickname);
            user.setCreatedAt(LocalDateTime.now());
            return repository.save(user);
        });
    }

    @GetMapping("/exists")
    public AppUser exists(@RequestParam String nickname) {
        return repository.findByNickname(nickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    @PutMapping("/{id}")
    public AppUser updateProfile(@PathVariable Long id, @RequestBody AppUser payload) {
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        String content = safe(payload.getAvatarUrl()) + " " + safe(payload.getBio()) + " " + safe(payload.getCity());
        if (PHONE_PATTERN.matcher(content).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "个人资料不能填写手机号，请使用站内私信");
        }
        user.setAvatarUrl(safe(payload.getAvatarUrl()));
        user.setBio(safe(payload.getBio()));
        user.setCity(safe(payload.getCity()));
        return repository.save(user);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

