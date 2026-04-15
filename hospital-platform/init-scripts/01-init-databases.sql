-- ============================================
-- MySQL 初始化脚本
-- 创建 neuroimmune 数据库
-- ============================================

-- 创建 neuroimmune 数据库
CREATE DATABASE IF NOT EXISTS neuroimmune
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 授权（如需要）
-- GRANT ALL PRIVILEGES ON neuroimmune.* TO 'root'@'%';
-- FLUSH PRIVILEGES;