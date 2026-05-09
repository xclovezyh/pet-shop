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
    /** 收藏ID。 */
    private Long id;

    /** 收藏人昵称。 */
    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    /** 交易帖ID。 */
    @Column(name = "post_id", nullable = false)
    private Long postId;
    /** 收藏时间。 */
    private LocalDateTime createdAt;

    /** 接口返回时附带的交易帖详情，不持久化。 */
    @Transient
    private MarketPost post;
}
