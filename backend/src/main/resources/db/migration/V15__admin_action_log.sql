CREATE TABLE admin_action_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计日志ID',
    admin_username VARCHAR(64) NOT NULL COMMENT '管理员用户名',
    action VARCHAR(64) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(64) NOT NULL COMMENT '操作对象类型',
    target_id BIGINT NULL COMMENT '操作对象ID',
    detail VARCHAR(1000) NULL COMMENT '操作说明',
    created_at DATETIME NOT NULL COMMENT '操作时间',
    INDEX idx_admin_action_logs_created_at (created_at),
    INDEX idx_admin_action_logs_admin_username (admin_username),
    INDEX idx_admin_action_logs_target (target_type, target_id)
) COMMENT='管理员操作审计日志';
