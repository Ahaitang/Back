-- 删除 patient.disease_type 字段
-- 该字段已迁移到 patient_disease 关联表

-- 先删除索引
ALTER TABLE patient DROP INDEX idx_patient_disease_type;
-- 再删除列
ALTER TABLE patient DROP COLUMN disease_type;