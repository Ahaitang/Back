-- 数据库迁移脚本：修复 admin 表并重置管理员账号
-- 执行此脚本前请确保已备份数据

USE neuroimmune;

-- 1. 添加 level 字段（如果不存在）
-- MySQL 不支持 IF NOT EXISTS 对于 ADD COLUMN，所以先检查再添加
-- 如果报错说字段已存在，可以忽略该错误

-- 查看是否需要添加 level 字段
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'neuroimmune'
    AND TABLE_NAME = 'admin'
    AND COLUMN_NAME = 'level'
);

-- 添加 level 字段
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE admin ADD COLUMN level INT DEFAULT 1 COMMENT ''管理员等级：1-超级管理员 2-普通管理员'' AFTER name',
    'SELECT ''level 字段已存在'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 删除旧的 admin 用户（如果存在）
DELETE FROM admin WHERE username = 'admin';

-- 3. 插入新的管理员账号
-- 密码 '123456' 的 BCrypt 加密值
INSERT INTO admin (username, password, name, level, create_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, NOW());

-- 提示
SELECT '迁移完成！管理员账号: admin / 123456' AS result;

ALTER TABLE medical_record ADD COLUMN notes VARCHAR(500) COMMENT '备注' AFTER attachments;


CREATE TABLE `disease_episode` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `patient_id` bigint DEFAULT NULL,
                                   `patient_name` varchar(100) DEFAULT NULL,
                                   `episode_number` int DEFAULT NULL COMMENT '发作次数',
                                   `episode_date` date DEFAULT NULL COMMENT '发作时间',
                                   `chief_complaint` varchar(500) DEFAULT NULL COMMENT '主诉',
                                   `symptoms` varchar(1000) DEFAULT NULL COMMENT '症状',
                                   `disease_progress` text COMMENT '病情变化过程',
                                   `treatment_process` text COMMENT '诊治经过',
                                   `diagnosis` varchar(500) DEFAULT NULL COMMENT '诊断结果',
                                   `hospital` varchar(200) DEFAULT NULL COMMENT '就诊医院',
                                   `department` varchar(100) DEFAULT NULL COMMENT '科室',
                                   `notes` text COMMENT '备注',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疾病发作记录';