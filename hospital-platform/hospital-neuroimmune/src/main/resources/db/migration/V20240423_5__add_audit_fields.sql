-- V20240423_5__add_audit_fields.sql
-- 为各实体表增加审计字段

-- Patient 表
ALTER TABLE patient ADD COLUMN created_by BIGINT COMMENT '创建者ID';
ALTER TABLE patient ADD COLUMN updated_by BIGINT COMMENT '修改者ID';

-- Doctor 表
ALTER TABLE doctor ADD COLUMN created_by BIGINT COMMENT '创建者ID';
ALTER TABLE doctor ADD COLUMN updated_by BIGINT COMMENT '修改者ID';

-- FollowUp 表
ALTER TABLE follow_up ADD COLUMN created_by BIGINT COMMENT '创建者ID';
ALTER TABLE follow_up ADD COLUMN updated_by BIGINT COMMENT '修改者ID';

-- Medication 表
ALTER TABLE medication ADD COLUMN created_by BIGINT COMMENT '创建者ID';
ALTER TABLE medication ADD COLUMN updated_by BIGINT COMMENT '修改者ID';

-- MedicalRecord 表（病历）
ALTER TABLE medical_record ADD COLUMN created_by BIGINT COMMENT '创建者ID';
ALTER TABLE medical_record ADD COLUMN updated_by BIGINT COMMENT '修改者ID';