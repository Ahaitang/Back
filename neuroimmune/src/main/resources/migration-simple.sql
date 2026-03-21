-- 简化版数据库迁移脚本
-- 请根据实际情况手动执行需要的语句

USE neuroimmune;

-- ============================================
-- admin 表添加 level 字段
-- ============================================
ALTER TABLE admin ADD COLUMN level INT DEFAULT 1 COMMENT '管理员等级：1-超级管理员 2-普通管理员';

-- ============================================
-- doctor 表添加 password 字段
-- ============================================
ALTER TABLE doctor ADD COLUMN password VARCHAR(100);

-- ============================================
-- patient 表添加 password 字段
-- ============================================
ALTER TABLE patient ADD COLUMN password VARCHAR(100);

-- ============================================
-- 更新已有数据的密码（BCrypt 加密的 123456）
-- ============================================
UPDATE doctor SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH' WHERE password IS NULL;
UPDATE patient SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH' WHERE password IS NULL;

-- ============================================
-- 查看表结构确认
-- ============================================
DESC admin;
DESC doctor;
DESC patient;