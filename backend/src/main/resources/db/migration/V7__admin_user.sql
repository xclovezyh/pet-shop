CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    enabled BIT(1) NOT NULL,
    created_at DATETIME(6),
    last_login_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_username (username)
);

CREATE TABLE IF NOT EXISTS admin_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_session_token_hash (token_hash),
    KEY idx_admin_session_admin_user_id (admin_user_id),
    KEY idx_admin_session_expires_at (expires_at)
);
