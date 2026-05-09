package com.petshop.model;

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
    private Long id;

    private String nickname;
    private String username;
    private String phone;
    private String passwordHash;
    private String passwordSalt;
    private String role;
    private String avatarUrl;
    private String city;
    private Boolean blacklisted;
    private String blacklistReason;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @javax.persistence.Column(length = 500)
    private String bio;
}

