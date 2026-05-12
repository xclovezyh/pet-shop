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
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的用户 ID。 */
    private Long userId;

    /** 令牌哈希值，仅服务端保存。 */
    @JsonIgnore
    private String tokenHash;

    /** 会话创建时间。 */
    private LocalDateTime createdAt;

    /** 会话过期时间。 */
    private LocalDateTime expiresAt;

    /** 最近一次使用时间。 */
    private LocalDateTime lastUsedAt;
}
