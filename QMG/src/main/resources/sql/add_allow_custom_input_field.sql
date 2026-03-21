-- ============================================
-- 为现有数据库添加 allow_custom_input 字段
-- 如果数据库已经存在，执行此脚本添加字段
-- ============================================

-- 为 questionnaire_option 表添加 allow_custom_input 字段
ALTER TABLE `questionnaire_option` 
ADD COLUMN `allow_custom_input` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许用户输入自定义数据（0=不允许，1=允许）' 
AFTER `display_order`;
