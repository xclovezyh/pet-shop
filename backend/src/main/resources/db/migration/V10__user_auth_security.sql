ALTER TABLE app_user
    MODIFY password_hash VARCHAR(255) COMMENT '用户密码单向哈希，前端提交 SM3 摘要，服务端再做 BCrypt 加盐哈希存储';

ALTER TABLE app_user
    MODIFY password_salt VARCHAR(255) COMMENT '历史保留字段，BCrypt 盐值已内嵌在 password_hash 中，不再单独保存可逆密码信息';

ALTER TABLE user_session
    COMMENT = '普通用户历史会话表，JWT 上线后仅用于兼容旧 token';
