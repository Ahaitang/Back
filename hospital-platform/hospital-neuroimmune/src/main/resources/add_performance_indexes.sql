-- ============================================
-- Neuroimmune 系统性能优化索引脚本
-- MySQL 版本（不支持 IF NOT EXISTS）
-- 如果索引已存在，会报错 "Duplicate key name"，可忽略
-- ============================================

USE neuroimmune;

-- follow_up 表优化索引
-- 用于按医生+状态+日期查询（最常用）
CREATE INDEX idx_fu_doctor_status_date ON follow_up(doctor_id, status, date);

-- 用于按患者+日期查询
CREATE INDEX idx_fu_patient_date ON follow_up(patient_id, date);

-- 用于统计待处理随访数量
CREATE INDEX idx_fu_status ON follow_up(status);

-- patient 表优化索引
-- 用于按医生查询患者
CREATE INDEX idx_patient_doctor_update ON patient(doctor_id, update_time);

-- 用于按手机号快速查找
CREATE INDEX idx_patient_phone ON patient(phone);

-- patient_doctor_relation 表优化索引
-- 用于检查绑定关系
CREATE INDEX idx_pdr_patient_status ON patient_doctor_relation(patient_id, status);

CREATE INDEX idx_pdr_doctor_status ON patient_doctor_relation(doctor_id, status);

-- medication 表优化索引
CREATE INDEX idx_med_patient_date ON medication(patient_id, date);

CREATE INDEX idx_med_doctor ON medication(doctor_id);