-- 数据库迁移脚本
-- 用于已有数据库的字段更新

USE neuroimmune;

-- ============================================
-- 1. admin 表添加 level 字段
-- ============================================
ALTER TABLE admin ADD COLUMN IF NOT EXISTS level INT DEFAULT 1 COMMENT '管理员等级：1-超级管理员 2-普通管理员';

-- 如果不支持 IF NOT EXISTS，使用以下方式：
-- ALTER TABLE admin ADD COLUMN level INT DEFAULT 1 COMMENT '管理员等级：1-超级管理员 2-普通管理员';

-- ============================================
-- 2. doctor 表添加 password 字段（如果不存在）
-- ============================================
-- MySQL 5.7+ 可以使用以下方式检查字段是否存在
SET @dbname = DATABASE();
SET @tablename = 'doctor';
SET @columnname = 'password';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(100) COMMENT "密码"')
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 3. patient 表添加 password 字段（如果不存在）
-- ============================================
SET @tablename = 'patient';
SET @columnname = 'password';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(100) COMMENT "密码"')
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 4. 更新已有医生密码（默认 123456）
-- ============================================
-- 注意：这是 BCrypt 加密后的 123456
UPDATE doctor SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'
WHERE password IS NULL OR password = '';

-- ============================================
-- 5. 更新已有患者密码（默认 123456）
-- ============================================
UPDATE patient SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'
WHERE password IS NULL OR password = '';

-- ============================================
-- 说明：
-- 1. admin 表新增 level 字段，用于区分管理员等级
--    - level=1: 超级管理员，拥有所有权限
--    - level=2: 普通管理员，权限受限
-- 2. doctor 和 patient 表添加 password 字段
-- 3. 系统启动时会自动创建 admin 账号（如果不存在）
--    - 用户名: admin
--    - 密码: 123456
-- ============================================