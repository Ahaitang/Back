-- medical_record: 添加 doctor_id 字段（原来只有 doctor_name，缺少关联 ID）
ALTER TABLE medical_record ADD COLUMN doctor_id BIGINT DEFAULT NULL COMMENT '医生ID' AFTER patient_id;

-- 添加索引
ALTER TABLE medical_record ADD INDEX idx_mr_patient_date (patient_id, date);
ALTER TABLE medical_record ADD INDEX idx_mr_doctor (doctor_id);
