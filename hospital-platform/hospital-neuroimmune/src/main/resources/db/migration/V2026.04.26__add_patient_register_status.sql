-- Patient 表添加 status 字段（INT类型）
ALTER TABLE patient ADD COLUMN status INT DEFAULT 1 COMMENT '患者状态: 0-待确认, 1-正常, 2-已拒绝, 3-禁用';

-- patient_doctor_relation 表添加绑定状态字段（INT类型）
ALTER TABLE patient_doctor_relation ADD COLUMN bind_status INT DEFAULT 1 COMMENT '绑定状态: 0-待确认, 1-已确认, 2-已拒绝';
ALTER TABLE patient_doctor_relation ADD COLUMN request_time DATETIME DEFAULT NOW() COMMENT '绑定请求时间';
ALTER TABLE patient_doctor_relation ADD COLUMN confirm_time DATETIME NULL COMMENT '确认/拒绝时间';

-- 更新现有数据：已存在的患者默认为1(正常)，已存在的绑定关系默认为1(已确认)
UPDATE patient SET status = 1 WHERE status IS NULL;
UPDATE patient_doctor_relation SET bind_status = 1, request_time = bind_time WHERE bind_status IS NULL;