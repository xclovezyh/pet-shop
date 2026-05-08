package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "post_favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_nickname", "post_id"}))
public class PostFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(name = "post_id", nullable = false)
    private Long postId;
    private LocalDateTime createdAt;

    @Transient
    private MarketPost post;
}
