package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.user.AuthSessionResponse;
import com.petshop.dto.user.LoginByPasswordRequest;
import com.petshop.dto.user.LoginBySmsRequest;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.ResetPasswordRequest;
import com.petshop.dto.user.UpdateUserProfileRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.dto.user.VerifyCodeResponse;
import com.petshop.model.AppUser;
import com.petshop.repository.AppUserRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.PageSupport;
import com.petshop.support.UserGuard;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,19}$");
    private static final Pattern PASSWORD_DIGEST_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");
    private static final String ROLE_USER = "USER";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final int PASSWORD_SALT_BYTES = 16;

    private final AppUserRepository users;
    private final UserJwtService userJwtService;
    private final Environment environment;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public UserService(AppUserRepository users,
                       UserJwtService userJwtService,
                       Environment environment) {
        this.users = users;
        this.userJwtService = userJwtService;
        this.environment = environment;
    }

    public PageResponse<UserResponse> list(String adminNickname, Integer page, Integer size) {
        int safePage = PageSupport.normalizePage(page);
        int safeSize = PageSupport.normalizeSize(size);
        Page<AppUser> pageResult = users.findByRoleNot(
                UserGuard.ROLE_SUPER_ADMIN,
                PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<UserResponse> response = new PageResponse<>();
        response.setItems(pageResult.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setTotal(pageResult.getTotalElements());
        response.setPage(safePage);
        response.setSize(safeSize);
        response.setTotalPages(pageResult.getTotalPages());
        response.setHasNext(pageResult.hasNext());
        response.setHasPrevious(pageResult.hasPrevious());
        return response;
    }

    public List<UserResponse> list(String adminNickname) {
        return list(adminNickname, 1, 50).getItems();
    }

    public VerifyCodeResponse sendVerifyCode(String phoneValue) {
        String phone = normalizePhone(phoneValue);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        verificationCodes.put(phone, new VerificationCode(code, LocalDateTime.now().plusMinutes(10)));

        VerifyCodeResponse response = new VerifyCodeResponse();
        response.setPhone(phone);
        response.setCode(isDevProfile() ? code : null);
        response.setExpireSeconds(600);
        response.setMessage("开发环境会直接返回验证码，接入短信服务后这里不会再返回 code 字段。");
        return response;
    }

    public AuthSessionResponse register(RegisterUserRequest request) {
        String username = normalizeUsername(request.getUsername());
        String passwordDigest = requirePasswordDigest(request.getPassword());
        String phone = normalizePhone(request.getPhone());
        String nickname = safe(request.getNickname());
        if (nickname.isEmpty()) {
            nickname = username;
        }
        if (PHONE_PATTERN.matcher(nickname).find()) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "昵称不能使用手机号");
        }
        ContentSafety.validate(nickname);
        verifyCode(phone, request.getCode());
        if (users.existsByUsername(username)) {
            throw new ApiException(ApiErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (users.existsByPhone(phone)) {
            throw new ApiException(ApiErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (users.existsByNickname(nickname)) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "昵称已被使用，请换一个昵称");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPhone(phone);
        user.setNickname(nickname);
        String salt = generatePasswordSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(hashPassword(passwordDigest, salt));
        user.setRole(ROLE_USER);
        user.setBlacklisted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        return buildAuthSession(users.save(user));
    }

    public AuthSessionResponse loginByPassword(LoginByPasswordRequest request) {
        String account = safe(request.getAccount());
        String passwordDigest = requirePasswordDigest(request.getPassword());
        if (account.isEmpty()) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "请输入账号和密码");
        }
        AppUser user = users.findByUsername(account)
                .orElseGet(() -> users.findByPhone(account)
                        .orElseThrow(() -> new ApiException(ApiErrorCode.LOGIN_FAILED)));
        if (UserGuard.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            throw new ApiException(ApiErrorCode.LOGIN_FAILED);
        }
        if (!passwordMatches(user, passwordDigest)) {
            throw new ApiException(ApiErrorCode.LOGIN_FAILED);
        }
        return buildAuthSession(finishLogin(user));
    }

    public AuthSessionResponse loginBySms(LoginBySmsRequest request) {
        String phone = normalizePhone(request.getPhone());
        verifyCode(phone, request.getCode());
        AppUser user = users.findByPhone(phone)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "手机号尚未注册，请先注册账号"));
        return buildAuthSession(finishLogin(user));
    }

    public AuthSessionResponse resetPassword(ResetPasswordRequest request) {
        String phone = normalizePhone(request.getPhone());
        String passwordDigest = requirePasswordDigest(request.getPassword());
        verifyCode(phone, request.getCode());
        AppUser user = users.findByPhone(phone)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "手机号尚未注册，请先注册账号"));
        String salt = generatePasswordSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(hashPassword(passwordDigest, salt));
        return buildAuthSession(finishLogin(users.save(user)));
    }

    public UserResponse me(AppUser currentUser) {
        return toResponse(currentUser);
    }

    public UserResponse exists(String nickname) {
        AppUser user = users.findByNickname(safe(nickname))
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    public UserResponse updateProfile(Long id, UpdateUserProfileRequest request) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
        UserGuard.requireActive(users, user.getNickname(), "维护个人资料");

        String content = safe(request.getAvatarUrl()) + " " + safe(request.getBio()) + " " + safe(request.getCity());
        if (PHONE_PATTERN.matcher(content).find()) {
            throw new ApiException(ApiErrorCode.INVALID_PARAM, "个人资料不能填写手机号，请使用站内私信");
        }
        ContentSafety.validate(content);
        user.setAvatarUrl(safe(request.getAvatarUrl()));
        user.setBio(safe(request.getBio()));
        user.setCity(safe(request.getCity()));
        return toResponse(users.save(user));
    }

    public UserResponse adminResetPassword(Long id, ResetPasswordRequest request, String adminNickname) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
        String passwordDigest = requirePasswordDigest(request == null ? null : request.getPassword());
        String salt = generatePasswordSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(hashPassword(passwordDigest, salt));
        user.setJwtToken("");
        user.setJwtTokenExpiresAt(null);
        return toResponse(users.save(user));
    }

    public UserResponse blacklist(Long id, String adminNickname, String reason) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
        if (UserGuard.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN, "不能拉黑超级管理员");
        }
        user.setBlacklisted(true);
        user.setJwtToken("");
        user.setJwtTokenExpiresAt(null);
        user.setBlacklistReason(isBlank(reason) ? "账号存在违规行为" : safe(reason));
        return toResponse(users.save(user));
    }

    public UserResponse unblacklist(Long id, String adminNickname) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
        user.setBlacklisted(false);
        user.setBlacklistReason("");
        return toResponse(users.save(user));
    }

    public UserResponse updateRole(Long id, String adminNickname, String role) {
        throw new ApiException(ApiErrorCode.FORBIDDEN, "普通用户与管理员账号已拆分，请在管理员后台创建管理员账号");
    }

    private AppUser finishLogin(AppUser user) {
        if (Boolean.TRUE.equals(user.getBlacklisted())) {
            throw new ApiException(ApiErrorCode.ACCOUNT_BLOCKED,
                    isBlank(user.getBlacklistReason()) ? ApiErrorCode.ACCOUNT_BLOCKED.getMessage() : user.getBlacklistReason());
        }
        user.setLastLoginAt(LocalDateTime.now());
        return users.save(user);
    }

    private AuthSessionResponse buildAuthSession(AppUser user) {
        LocalDateTime issuedAt = LocalDateTime.now();
        String token = userJwtService.createToken(user, issuedAt);
        user.setJwtToken(token);
        user.setJwtTokenExpiresAt(userJwtService.expiresAt(issuedAt));
        AppUser saved = users.save(user);
        AuthSessionResponse response = new AuthSessionResponse();
        response.setToken(token);
        response.setUser(toResponse(saved));
        return response;
    }

    public void logout(String token) {
        userJwtService.resolveUser(token).ifPresent(user -> {
            user.setJwtToken("");
            user.setJwtTokenExpiresAt(null);
            users.save(user);
        });
    }

    private void verifyCode(String phone, String code) {
        VerificationCode saved = verificationCodes.get(phone);
        if (saved == null || saved.expiresAt.isBefore(LocalDateTime.now()) || !saved.code.equals(safe(code))) {
            throw new ApiException(ApiErrorCode.INVALID_VERIFY_CODE);
        }
        verificationCodes.remove(phone);
    }

    private String normalizePhone(String value) {
        String phone = safe(value).replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ApiException(ApiErrorCode.INVALID_PHONE);
        }
        if (phone.startsWith("+86")) {
            return phone.substring(3);
        }
        return phone;
    }

    private String normalizeUsername(String value) {
        String username = safe(value);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ApiException(ApiErrorCode.INVALID_USERNAME);
        }
        return username;
    }

    private String requirePasswordDigest(String value) {
        String passwordDigest = safe(value);
        if (!PASSWORD_DIGEST_PATTERN.matcher(passwordDigest).matches()) {
            throw new ApiException(ApiErrorCode.INVALID_PASSWORD, "密码参数格式不正确，请使用前端 SM3 摘要后再提交");
        }
        return passwordDigest.toLowerCase();
    }

    private String generatePasswordSalt() {
        byte[] bytes = new byte[PASSWORD_SALT_BYTES];
        RANDOM.nextBytes(bytes);
        return String.format("%032x", new BigInteger(1, bytes));
    }

    private String hashPassword(String passwordDigest, String salt) {
        return PASSWORD_ENCODER.encode(saltedPassword(passwordDigest, salt));
    }

    private String saltedPassword(String passwordDigest, String salt) {
        return safe(salt) + ":" + passwordDigest;
    }

    private boolean passwordMatches(AppUser user, String passwordDigest) {
        if (isBlank(user.getPasswordHash())) {
            return false;
        }
        if (!isBlank(user.getPasswordSalt())) {
            return PASSWORD_ENCODER.matches(saltedPassword(passwordDigest, user.getPasswordSalt()), user.getPasswordHash());
        }
        if (!PASSWORD_ENCODER.matches(passwordDigest, user.getPasswordHash())) {
            return false;
        }
        String salt = generatePasswordSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(hashPassword(passwordDigest, salt));
        users.save(user);
        return true;
    }

    private UserResponse toResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setNickname(user.getNickname());
        response.setUsername(user.getUsername());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCity(user.getCity());
        response.setBlacklisted(user.getBlacklisted());
        response.setBlacklistReason(user.getBlacklistReason());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setBio(user.getBio());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isDevProfile() {
        String[] profiles = environment == null ? new String[0] : environment.getActiveProfiles();
        return Arrays.stream(profiles == null ? new String[0] : profiles)
                .anyMatch(profile -> "dev".equalsIgnoreCase(profile));
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
