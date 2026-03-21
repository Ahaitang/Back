-- 患者-医生绑定关系表
-- 执行此脚本创建绑定关系表

-- 创建绑定关系表
CREATE TABLE IF NOT EXISTS patient_doctor_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名（冗余）',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(50) COMMENT '医生姓名（冗余）',
    relation_type VARCHAR(20) DEFAULT 'primary' COMMENT '关系类型：primary-主治医生, consultant-会诊医生',
    status VARCHAR(20) DEFAULT 'active' COMMENT '绑定状态：active-生效中, inactive-已解绑',
    bind_method VARCHAR(20) DEFAULT 'patient' COMMENT '绑定方式：system-系统分配, patient-患者选择, doctor-医生邀请',
    remark VARCHAR(255) COMMENT '备注',
    bind_time DATETIME COMMENT '绑定时间',
    unbind_time DATETIME COMMENT '解绑时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者-医生绑定关系表';

-- 从现有患者表迁移数据到绑定关系表
-- 将已有绑定关系的患者创建对应的绑定记录
INSERT INTO patient_doctor_relation (patient_id, patient_name, doctor_id, doctor_name, relation_type, status, bind_method, bind_time, create_time)
SELECT id, name, doctor_id, doctor_name, 'primary', 'active', 'system', create_time, create_time
FROM patient
WHERE doctor_id IS NOT NULL AND doctor_id > 0;

-- 查看迁移结果
SELECT COUNT(*) as '已迁移绑定记录数' FROM patient_doctor_relation;