-- ============================================
-- QMG 系统性能优化索引脚本
-- MySQL 版本（不支持 IF NOT EXISTS）
-- 如果索引已存在，会报错 "Duplicate key name"，可忽略
-- ============================================

USE QMG;

-- questionnaire_record 表优化索引
-- 用于按患者+日期查询（最常用）
CREATE INDEX idx_qr_patient_date ON questionnaire_record(patient_id, assessment_date);

-- 用于按医生查询问卷记录
CREATE INDEX idx_qr_doctor_create ON questionnaire_record(doctor_id, create_time);

-- 用于按住院号+日期查询
CREATE INDEX idx_qr_admission_date ON questionnaire_record(admission_number, assessment_date);

-- 用于按测评日期统计
CREATE INDEX idx_qr_assessment_date ON questionnaire_record(assessment_date);

-- patient_doctor 表优化索引
-- 用于检查患者-医生关系是否存在
CREATE INDEX idx_pd_patient_doctor ON patient_doctor(patient_id, doctor_id);