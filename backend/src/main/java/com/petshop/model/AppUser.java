package com.petshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 用户ID。 */
    private Long id;

    /** 站内展示昵称，用于发布、私信和管理操作展示。 */
    private String nickname;
    /** 登录用户名。 */
    private String username;
    /** 登录手机号。 */
    private String phone;
    /** BCrypt 密码哈希。 */
    @JsonIgnore
    private String passwordHash;
    /** 预留密码盐字段，BCrypt 已内置盐。 */
    @JsonIgnore
    private String passwordSalt;
    @JsonIgnore
    private String jwtToken;
    @JsonIgnore
    private LocalDateTime jwtTokenExpiresAt;
    /** 用户角色：USER 普通用户，SUPER_ADMIN 超级管理员。 */
    private String role;
    /** 头像图片地址。 */
    private String avatarUrl;
    /** 常驻城市名称。 */
    private String city;
    /** 常驻城市行政区划代码。 */
    private String cityCode;
    /** 是否被平台限制。 */
    private Boolean blacklisted;
    /** 账号限制原因。 */
    private String blacklistReason;
    /** 注册时间。 */
    private LocalDateTime createdAt;
    /** 最近登录时间。 */
    private LocalDateTime lastLoginAt;

    /** 个人简介。 */
    @javax.persistence.Column(length = 500)
    private String bio;
}

