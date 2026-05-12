CREATE TABLE user_session (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    token_hash VARCHAR(128) NOT NULL COMMENT '令牌哈希',
    created_at DATETIME(6) NOT NULL COMMENT '创建时间',
    expires_at DATETIME(6) NOT NULL COMMENT '过期时间',
    last_used_at DATETIME(6) NOT NULL COMMENT '最近使用时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_session_token_hash (token_hash),
    KEY idx_user_session_user_id (user_id),
    KEY idx_user_session_expires_at (expires_at)
) COMMENT='用户登录会话表';
