-- 将 patient_doctor_relation.status 从 varchar 转为 tinyint
-- 与其他表的 status 字段统一为整数类型

-- Step 1: 添加新的整数列
ALTER TABLE patient_doctor_relation ADD COLUMN status_int TINYINT DEFAULT 1 COMMENT '绑定状态：1-生效中, 0-已解绑';

-- Step 2: 迁移数据
UPDATE patient_doctor_relation SET status_int = 1 WHERE status = 'active';
UPDATE patient_doctor_relation SET status_int = 0 WHERE status = 'inactive' OR status IS NULL;

-- Step 3: 删除旧列，重命名新列
ALTER TABLE patient_doctor_relation DROP COLUMN status;
ALTER TABLE patient_doctor_relation CHANGE COLUMN status_int status TINYINT DEFAULT 1 COMMENT '绑定状态：1-生效中, 0-已解绑';