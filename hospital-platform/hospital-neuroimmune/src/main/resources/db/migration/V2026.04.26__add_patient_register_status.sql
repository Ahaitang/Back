-- Patient 表添加 status 字段
ALTER TABLE patient ADD COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT '患者状态: pending-待确认, active-正常, rejected-已拒绝, inactive-禁用';

-- patient_doctor_relation 表添加绑定状态字段
ALTER TABLE patient_doctor_relation ADD COLUMN bind_status VARCHAR(20) DEFAULT 'confirmed' COMMENT '绑定状态: pending-待确认, confirmed-已确认, rejected-已拒绝';
ALTER TABLE patient_doctor_relation ADD COLUMN request_time DATETIME DEFAULT NOW() COMMENT '绑定请求时间';
ALTER TABLE patient_doctor_relation ADD COLUMN confirm_time DATETIME NULL COMMENT '确认/拒绝时间';

-- 更新现有数据：已存在的患者默认为active，已存在的绑定关系默认为confirmed
UPDATE patient SET status = 'active' WHERE status IS NULL;
UPDATE patient_doctor_relation SET bind_status = 'confirmed', request_time = bind_time WHERE bind_status IS NULL;