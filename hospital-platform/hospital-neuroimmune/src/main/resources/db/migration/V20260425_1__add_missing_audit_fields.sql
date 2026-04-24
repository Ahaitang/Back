-- 补全所有表缺失的审计字段 (update_time, is_deleted)

-- dict_common: 缺少 is_deleted
ALTER TABLE dict_common ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';

-- disease_episode: 缺少 update_time (is_deleted 已在 V20240424_1 添加)
ALTER TABLE disease_episode ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- doctor: 缺少 update_time
ALTER TABLE doctor ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- doctor_role: 缺少 update_time, is_deleted
ALTER TABLE doctor_role ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE doctor_role ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';

-- follow_up: 缺少 update_time, is_deleted
ALTER TABLE follow_up ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE follow_up ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';

-- medical_record: 缺少 update_time, is_deleted
ALTER TABLE medical_record ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE medical_record ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';

-- medication: 缺少 update_time, is_deleted
ALTER TABLE medication ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE medication ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';

-- patient_doctor_relation: 缺少 update_time, is_deleted
ALTER TABLE patient_doctor_relation ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE patient_doctor_relation ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';
