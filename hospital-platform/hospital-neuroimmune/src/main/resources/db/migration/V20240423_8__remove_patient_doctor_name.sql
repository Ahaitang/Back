-- 移除 patient 表中的冗余字段 doctor_name
-- 医生姓名应该通过 doctor_id 关联查询 doctor 表获取
ALTER TABLE patient DROP COLUMN doctor_name;