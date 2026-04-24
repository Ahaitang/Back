-- V20240423_6__create_doctor_role_table.sql
-- 创建医生角色关联表，并迁移现有数据

CREATE TABLE doctor_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    is_active INT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_role_code (role_code),
    UNIQUE KEY uk_doctor_role (doctor_id, role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生角色关联表';

INSERT INTO doctor_role (doctor_id, role_code, is_active)
SELECT id, 'DOCTOR', 1 FROM doctor WHERE role LIKE '%doctor%' OR role IS NULL OR role = '';

INSERT INTO doctor_role (doctor_id, role_code, is_active)
SELECT id, 'ADMIN', 1 FROM doctor WHERE role LIKE '%admin%';

INSERT INTO doctor_role (doctor_id, role_code, is_active)
SELECT id, 'DOCTOR_ADMIN', 1 FROM doctor WHERE role LIKE '%doctor%admin%' OR role LIKE '%admin%doctor%';