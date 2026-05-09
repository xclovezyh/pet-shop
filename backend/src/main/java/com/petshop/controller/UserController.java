package com.petshop.controller;

import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,19}$");
    private static final String ROLE_USER = "USER";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AppUserRepository repository;
    private final List<String> adminNicknames;
    private final String adminCode;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

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

    @PostMapping("/verification-code")
    public Map<String, String> verificationCode(@RequestBody Map<String, String> payload) {
        String phone = normalizePhone(payload.get("phone"));
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        verificationCodes.put(phone, new VerificationCode(code, LocalDateTime.now().plusMinutes(10)));
        return new java.util.LinkedHashMap<String, String>() {{
            put("phone", phone);
            put("code", code);
            put("expireSeconds", "600");
            put("message", "开发环境已直接返回验证码，接入短信服务后这里不再回传 code。");
        }};
    }

    @PostMapping("/register")
    public AppUser register(@RequestBody Map<String, String> payload) {
        String username = normalizeUsername(payload.get("username"));
        String password = requirePassword(payload.get("password"));
        String phone = normalizePhone(payload.get("phone"));
        String nickname = safe(payload.get("nickname"));
        if (nickname.isEmpty()) {
            nickname = username;
        }
        if (PHONE_PATTERN.matcher(nickname).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "昵称不能使用手机号");
        }
        if (isAdminNickname(nickname) || isAdminNickname(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "超级管理员账号不能在前台注册，请使用密码登录并填写管理员口令完成初始化");
        }
        ContentSafety.validate(nickname);
        verifyCode(phone, payload.get("code"));
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已被注册");
        }
        if (repository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "手机号已被注册");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPhone(phone);
        user.setNickname(nickname);
        setPassword(user, password);
        user.setRole(ROLE_USER);
        user.setBlacklisted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        return repository.save(user);
    }

    @PostMapping("/login/password")
    public AppUser passwordLogin(@RequestBody Map<String, String> payload) {
        String account = safe(payload.get("account"));
        String password = safe(payload.get("password"));
        if (account.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入账号和密码");
        }
        AppUser user = repository.findByUsername(account)
                .orElseGet(() -> repository.findByPhone(account)
                        .orElseGet(() -> createAdminOnFirstLogin(account, password, payload.get("adminCode"))));
        if (isBlank(user.getPasswordHash()) || !PASSWORD_ENCODER.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        maybePromoteAdmin(user, payload.get("adminCode"));
        return finishLogin(user);
    }

    @PostMapping("/login/sms")
    public AppUser smsLogin(@RequestBody Map<String, String> payload) {
        String phone = normalizePhone(payload.get("phone"));
        verifyCode(phone, payload.get("code"));
        AppUser user = repository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "手机号尚未注册，请先注册账号"));
        maybePromoteAdmin(user, payload.get("adminCode"));
        return finishLogin(user);
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
            created.setUsername(finalNickname);
            created.setRole(ROLE_USER);
            created.setBlacklisted(false);
            created.setCreatedAt(LocalDateTime.now());
            return repository.save(created);
        });
        maybePromoteAdmin(user, payload.get("adminCode"));
        return finishLogin(user);
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

    private AppUser createAdminOnFirstLogin(String account, String password, String suppliedAdminCode) {
        if (!isAdminNickname(account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (!adminCode.equals(safe(suppliedAdminCode))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号需要填写正确的管理员口令");
        }
        AppUser user = new AppUser();
        user.setUsername(account);
        user.setNickname(account);
        user.setRole(UserGuard.ROLE_SUPER_ADMIN);
        user.setBlacklisted(false);
        user.setCreatedAt(LocalDateTime.now());
        setPassword(user, password);
        return repository.save(user);
    }

    private void maybePromoteAdmin(AppUser user, String suppliedAdminCode) {
        if (!isAdminNickname(user.getNickname())) {
            if (isBlank(user.getRole())) {
                user.setRole(ROLE_USER);
            }
            return;
        }
        if (!adminCode.equals(safe(suppliedAdminCode))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号需要填写正确的管理员口令");
        }
        user.setRole(UserGuard.ROLE_SUPER_ADMIN);
    }

    private AppUser finishLogin(AppUser user) {
        if (Boolean.TRUE.equals(user.getBlacklisted())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, safe(user.getBlacklistReason()).isEmpty() ? "账号已被限制，暂不能登录" : user.getBlacklistReason());
        }
        user.setLastLoginAt(LocalDateTime.now());
        return repository.save(user);
    }

    private void setPassword(AppUser user, String password) {
        user.setPasswordSalt("");
        user.setPasswordHash(PASSWORD_ENCODER.encode(password));
    }

    private void verifyCode(String phone, String code) {
        VerificationCode saved = verificationCodes.get(phone);
        if (saved == null || saved.expiresAt.isBefore(LocalDateTime.now()) || !saved.code.equals(safe(code))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码不正确或已过期");
        }
        verificationCodes.remove(phone);
    }

    private String normalizePhone(String value) {
        String phone = safe(value).replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入正确的手机号");
        }
        if (phone.startsWith("+86")) {
            phone = phone.substring(3);
        }
        return phone;
    }

    private String normalizeUsername(String value) {
        String username = safe(value);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名需以字母开头，4-20 位，仅支持字母、数字和下划线");
        }
        return username;
    }

    private String requirePassword(String value) {
        String password = safe(value);
        if (password.length() < 6 || password.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码长度需为 6-64 位");
        }
        return password;
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

    private static class VerificationCode {
        private final String code;
        private final LocalDateTime expiresAt;

        private VerificationCode(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
