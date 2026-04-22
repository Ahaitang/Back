-- ============================================
-- 04. Session Audit 数据库结构
-- 会话审计系统（跨模块共享）
-- 执行顺序: 第五个执行（在 03-neuroimmune-schema.sql 之后）
-- ============================================

USE session_audit;

-- ============================================
-- 会话审计日志表
-- ============================================
CREATE TABLE IF NOT EXISTS session_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：doctor/admin/patient',
    module VARCHAR(20) NOT NULL COMMENT '模块：qmg/neuroimmune',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    logout_time DATETIME COMMENT '登出时间',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '浏览器User-Agent',
    device VARCHAR(100) COMMENT '解析后的设备信息',
    duration_seconds INT COMMENT '会话时长（秒）',
    operation_type VARCHAR(20) COMMENT '操作类型：LOGIN/LOGOUT/KICK_OFFLINE',
    operator_id BIGINT COMMENT '踢下线操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id, role, module),
    INDEX idx_login_time (login_time),
    INDEX idx_operation (operation_type)
) ENGINE=InnoDB COMMENT='会话审计日志';

-- ============================================
-- 黑名单表
-- ============================================
CREATE TABLE IF NOT EXISTS black_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    module VARCHAR(20) NOT NULL COMMENT '模块',
    reason VARCHAR(255) COMMENT '封禁原因',
    ban_time DATETIME NOT NULL COMMENT '封禁时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/released',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME,
    INDEX idx_user (user_id, role, module),
    INDEX idx_status (status),
    INDEX idx_expire (expire_time)
) ENGINE=InnoDB COMMENT='黑名单';

-- ============================================
-- 系统配置表
-- ============================================
CREATE TABLE IF NOT EXISTS system_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME
) ENGINE=InnoDB COMMENT='系统配置';

-- ============================================
-- 超级管理员账号表
-- ============================================
CREATE TABLE IF NOT EXISTS super_admin_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    name VARCHAR(100) COMMENT '显示名称',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB COMMENT='超级管理员账号';

-- ============================================
-- 初始配置数据
-- ============================================
INSERT INTO system_config (config_key, config_value, description) VALUES
('qmg_token_hours', '24', 'QMG系统Token有效期（小时）'),
('neuro_token_hours', '24', 'Neuroimmune系统Token有效期（小时）'),
('default_ban_hours', '24', '默认封禁时长（小时）'),
('max_ban_hours', '72', '最大封禁时长（小时）'),
('kick_confirm_enabled', 'true', '踢下线确认开关')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 提示
SELECT 'Session Audit 数据库结构创建完成！' AS status;