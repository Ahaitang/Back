-- V20240423_9__optimize_patient_age_and_audit.sql
-- 优化 Patient 表：删除审计字段，将 age 改为 birth_date

-- 删除未使用的审计字段
ALTER TABLE patient DROP COLUMN created_by;
ALTER TABLE patient DROP COLUMN updated_by;

-- 添加出生日期字段
ALTER TABLE patient ADD COLUMN birth_date DATE COMMENT '出生日期';

-- 迁移现有年龄数据（估算出生年份，误差约±1年）
UPDATE patient SET birth_date = DATE_SUB(CURDATE(), INTERVAL age YEAR) WHERE age IS NOT NULL;

-- 删除静态 age 字段
ALTER TABLE patient DROP COLUMN age;

-- 删除其他表的审计字段
ALTER TABLE doctor DROP COLUMN created_by;
ALTER TABLE doctor DROP COLUMN updated_by;

ALTER TABLE follow_up DROP COLUMN created_by;
ALTER TABLE follow_up DROP COLUMN updated_by;

ALTER TABLE medication DROP COLUMN created_by;
ALTER TABLE medication DROP COLUMN updated_by;

ALTER TABLE medical_record DROP COLUMN created_by;
ALTER TABLE medical_record DROP COLUMN updated_by;