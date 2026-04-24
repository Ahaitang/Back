-- 补全缺失索引

-- disease_episode: is_deleted 索引（idx_de_patient_date 已存在）
ALTER TABLE disease_episode ADD INDEX idx_de_is_deleted (is_deleted);

-- doctor: 电话索引用于登录查询
ALTER TABLE doctor ADD INDEX idx_doctor_phone (phone);

-- patient: is_deleted 索引用于过滤查询；disease_type 索引（临时，后续删除 disease_type 列时会移除）
ALTER TABLE patient ADD INDEX idx_patient_is_deleted (is_deleted);
ALTER TABLE patient ADD INDEX idx_patient_disease_type (disease_type);

-- follow_up: is_deleted 索引
ALTER TABLE follow_up ADD INDEX idx_fu_is_deleted (is_deleted);

-- medical_record: is_deleted 索引
ALTER TABLE medical_record ADD INDEX idx_mr_is_deleted (is_deleted);

-- medication: is_deleted 索引
ALTER TABLE medication ADD INDEX idx_med_is_deleted (is_deleted);

-- patient_doctor_relation: is_deleted 索引
ALTER TABLE patient_doctor_relation ADD INDEX idx_pdr_is_deleted (is_deleted);