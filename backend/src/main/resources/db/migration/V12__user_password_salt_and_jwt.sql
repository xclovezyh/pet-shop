ALTER TABLE app_user
    ADD COLUMN jwt_token VARCHAR(1000) NULL COMMENT '普通用户当前 JWT token，登录成功后写入，退出或重新登录时更新';

ALTER TABLE app_user
    ADD COLUMN jwt_token_expires_at DATETIME NULL COMMENT '普通用户当前 JWT token 过期时间，默认登录后 6 小时';

ALTER TABLE app_user
    MODIFY password_salt VARCHAR(64) NULL COMMENT '每个普通用户独立密码盐，由后端首次设置密码时生成并存储';

ALTER TABLE app_user
    MODIFY password_hash VARCHAR(255) NULL COMMENT '前端 SM3 摘要与用户独立盐组合后，由后端 BCrypt 单向哈希存储';
