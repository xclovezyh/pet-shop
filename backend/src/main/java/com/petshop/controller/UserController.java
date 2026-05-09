package com.petshop.controller;

import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");
    private static final String ROLE_USER = "USER";

    private final AppUserRepository repository;
    private final List<String> adminNicknames;
    private final String adminCode;

    public UserController(AppUserRepository repository,
                          @Value("${app.admin-nicknames:superadmin}") String adminNicknames,
                          @Value("${app.admin-code:change-me-admin-code}") String adminCode) {
        this.repository = repository;
        this.adminNicknames = Arrays.asList(adminNicknames.split(","));
        this.adminCode = adminCode;
    }

    @GetMapping
    public List<AppUser> list(@RequestParam String admin) {
        UserGuard.requireSuperAdmin(repository, admin);
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
        AppUser user = repository.findByNickname(nickname).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setNickname(finalNickname);
            created.setRole(ROLE_USER);
            created.setBlacklisted(false);
            created.setCreatedAt(LocalDateTime.now());
            return repository.save(created);
        });
        boolean adminNickname = isAdminNickname(nickname);
        if (adminNickname) {
            String code = safe(payload.get("adminCode"));
            if (!adminCode.equals(code)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号需要填写正确的管理员口令");
            }
            if (!UserGuard.ROLE_SUPER_ADMIN.equals(user.getRole())) {
                user.setRole(UserGuard.ROLE_SUPER_ADMIN);
                user = repository.save(user);
            }
        } else if (isBlank(user.getRole())) {
            user.setRole(ROLE_USER);
            user = repository.save(user);
        }
        if (Boolean.TRUE.equals(user.getBlacklisted())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, safe(user.getBlacklistReason()).isEmpty() ? "账号已被限制，暂不能登录" : user.getBlacklistReason());
        }
        return user;
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
        UserGuard.requireActive(repository, user.getNickname(), "维护个人资料");
        String content = safe(payload.getAvatarUrl()) + " " + safe(payload.getBio()) + " " + safe(payload.getCity());
        if (PHONE_PATTERN.matcher(content).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "个人资料不能填写手机号，请使用站内私信");
        }
        ContentSafety.validate(content);
        user.setAvatarUrl(safe(payload.getAvatarUrl()));
        user.setBio(safe(payload.getBio()));
        user.setCity(safe(payload.getCity()));
        return repository.save(user);
    }

    @PutMapping("/{id}/blacklist")
    public AppUser blacklist(@PathVariable Long id, @RequestParam String admin, @RequestParam(defaultValue = "账号存在违规行为") String reason) {
        UserGuard.requireSuperAdmin(repository, admin);
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (UserGuard.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能拉黑超级管理员");
        }
        user.setBlacklisted(true);
        user.setBlacklistReason(safe(reason).isEmpty() ? "账号存在违规行为" : safe(reason));
        return repository.save(user);
    }

    @PutMapping("/{id}/unblacklist")
    public AppUser unblacklist(@PathVariable Long id, @RequestParam String admin) {
        UserGuard.requireSuperAdmin(repository, admin);
        AppUser user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        user.setBlacklisted(false);
        user.setBlacklistReason("");
        return repository.save(user);
    }

    private boolean isAdminNickname(String nickname) {
        return adminNicknames.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .anyMatch(value -> value.equals(nickname));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

