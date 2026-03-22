-- ============================================
-- neuroimmune 数据库完整迁移脚本
-- 日期: 2026-03-21
-- 说明: 修复数据库字段缺失问题
-- 执行方式: 在 MySQL 客户端中逐条执行
-- ============================================

USE neuroimmune;

-- ============================================
-- 第一部分: patient 表
-- ============================================

-- 1.1 添加 disease_type 字段（疾病分类）
-- 如果报错 "Duplicate column name"，说明字段已存在，跳过即可
ALTER TABLE patient ADD COLUMN disease_type VARCHAR(50) DEFAULT NULL COMMENT '疾病分类' AFTER doctor_name;

-- 1.2 更新患者疾病分类示例数据
UPDATE patient SET disease_type = 'MS' WHERE id = 1;
UPDATE patient SET disease_type = 'NMOSD' WHERE id = 2;
UPDATE patient SET disease_type = 'GBS' WHERE id = 3;
UPDATE patient SET disease_type = 'MG' WHERE id = 5;
UPDATE patient SET disease_type = 'CIDP' WHERE id = 7;

-- ============================================
-- 第二部分: follow_up 表
-- ============================================

-- 2.1 添加 outpatient_time 字段（门诊时间）
-- 如果报错 "Duplicate column name"，说明字段已存在，跳过即可
ALTER TABLE follow_up ADD COLUMN outpatient_time DATETIME DEFAULT NULL COMMENT '门诊时间' AFTER content;

-- 2.2 添加 hospitalization_time 字段（住院时间）
ALTER TABLE follow_up ADD COLUMN hospitalization_time DATE DEFAULT NULL COMMENT '住院时间' AFTER outpatient_time;

-- 2.3 添加 examination_items 字段（检查项目）
ALTER TABLE follow_up ADD COLUMN examination_items TEXT DEFAULT NULL COMMENT '检查项目' AFTER hospitalization_time;

-- 2.4 添加 hospital 字段（医院）
ALTER TABLE follow_up ADD COLUMN hospital VARCHAR(100) DEFAULT NULL COMMENT '医院' AFTER examination_items;

-- 2.5 添加 department 字段（科室）
ALTER TABLE follow_up ADD COLUMN department VARCHAR(50) DEFAULT NULL COMMENT '科室' AFTER hospital;

-- 2.6 添加 notes 字段（备注）
ALTER TABLE follow_up ADD COLUMN notes TEXT DEFAULT NULL COMMENT '备注' AFTER department;

-- ============================================
-- 第三部分: patient_doctor_relation 表（可选）
-- ============================================

-- 3.1 创建患者-医生绑定关系表（如果不存在）
CREATE TABLE IF NOT EXISTS patient_doctor_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(50) COMMENT '医生姓名',
    relation_type VARCHAR(20) DEFAULT 'primary' COMMENT '关系类型',
    status VARCHAR(20) DEFAULT 'active' COMMENT '绑定状态',
    bind_method VARCHAR(20) DEFAULT 'patient' COMMENT '绑定方式',
    remark VARCHAR(255) COMMENT '备注',
    bind_time DATETIME COMMENT '绑定时间',
    unbind_time DATETIME COMMENT '解绑时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者-医生绑定关系表';

-- ============================================
-- 验证
-- ============================================

-- 查看 patient 表结构
DESCRIBE patient;

-- 查看 follow_up 表结构
DESCRIBE follow_up;

-- 查看统计
SELECT '迁移完成!' AS status;
SELECT COUNT(*) AS patient_count FROM patient;
SELECT COUNT(*) AS followup_count FROM follow_up;
SELECT COUNT(*) AS doctor_count FROM doctor;