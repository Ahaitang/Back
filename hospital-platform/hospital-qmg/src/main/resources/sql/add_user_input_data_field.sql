-- ============================================
-- 为现有数据库添加用户自定义输入数据字段
-- 如果数据库已经存在，执行此脚本添加字段
-- ============================================

-- 为 questionnaire_record 表添加 user_input_data 字段
ALTER TABLE `questionnaire_record` 
ADD COLUMN `user_input_data` TEXT DEFAULT NULL COMMENT '用户自定义输入数据（JSON格式，如备注、说明等）' 
AFTER `modified_by`;

-- 添加索引（可选，如果需要根据用户输入数据搜索）
-- ALTER TABLE `questionnaire_record` ADD INDEX `idx_user_input_data` (`user_input_data`(100));
