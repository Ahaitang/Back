-- 为所有表和字段添加注释说明

-- ============================================
-- 表注释
-- ============================================

ALTER TABLE doctor COMMENT '医生信息表';
ALTER TABLE follow_up COMMENT '随访记录表';
ALTER TABLE medical_record COMMENT '病历记录表';
ALTER TABLE medication COMMENT '用药记录表';
ALTER TABLE patient COMMENT '患者信息表';

-- ============================================
-- dict_common 字段注释
-- ============================================

ALTER TABLE dict_common MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE dict_common MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE dict_common MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- disease_episode 字段注释
-- ============================================

ALTER TABLE disease_episode MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE disease_episode MODIFY COLUMN patient_id BIGINT COMMENT '患者ID';
ALTER TABLE disease_episode MODIFY COLUMN episode_date DATETIME COMMENT '发作时间';
ALTER TABLE disease_episode MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE disease_episode MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- doctor 字段注释
-- ============================================

ALTER TABLE doctor MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE doctor MODIFY COLUMN name VARCHAR(50) COMMENT '医生姓名';
ALTER TABLE doctor MODIFY COLUMN title VARCHAR(50) COMMENT '职称';
ALTER TABLE doctor MODIFY COLUMN department VARCHAR(50) COMMENT '科室';
ALTER TABLE doctor MODIFY COLUMN hospital VARCHAR(100) COMMENT '所属医院';
ALTER TABLE doctor MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号';
ALTER TABLE doctor MODIFY COLUMN avatar VARCHAR(255) COMMENT '头像URL';
ALTER TABLE doctor MODIFY COLUMN password VARCHAR(100) COMMENT '密码（加密存储）';
ALTER TABLE doctor MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE doctor MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE doctor MODIFY COLUMN level INT COMMENT '管理等级，1最高，null表示普通医生';

-- ============================================
-- doctor_role 字段注释
-- ============================================

ALTER TABLE doctor_role MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE doctor_role MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE doctor_role MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- follow_up 字段注释
-- ============================================

ALTER TABLE follow_up MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE follow_up MODIFY COLUMN patient_id BIGINT COMMENT '患者ID';
ALTER TABLE follow_up MODIFY COLUMN doctor_id BIGINT COMMENT '医生ID';
ALTER TABLE follow_up MODIFY COLUMN date DATETIME COMMENT '随访日期';
ALTER TABLE follow_up MODIFY COLUMN project VARCHAR(100) COMMENT '随访项目';
ALTER TABLE follow_up MODIFY COLUMN type VARCHAR(50) COMMENT '随访类型';
ALTER TABLE follow_up MODIFY COLUMN content TEXT COMMENT '随访内容';
ALTER TABLE follow_up MODIFY COLUMN hospitalization_time DATETIME COMMENT '住院时间';
ALTER TABLE follow_up MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE follow_up MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- medical_record 字段注释
-- ============================================

ALTER TABLE medical_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE medical_record MODIFY COLUMN patient_id BIGINT COMMENT '患者ID';
ALTER TABLE medical_record MODIFY COLUMN type VARCHAR(50) COMMENT '病历类型：门诊病历、住院病历、外院病历';
ALTER TABLE medical_record MODIFY COLUMN diagnosis VARCHAR(200) COMMENT '诊断结果';
ALTER TABLE medical_record MODIFY COLUMN hospital VARCHAR(100) COMMENT '就诊医院';
ALTER TABLE medical_record MODIFY COLUMN department VARCHAR(50) COMMENT '科室';
ALTER TABLE medical_record MODIFY COLUMN date DATETIME COMMENT '病历日期';
ALTER TABLE medical_record MODIFY COLUMN content TEXT COMMENT '病历内容';
ALTER TABLE medical_record MODIFY COLUMN attachments TEXT COMMENT '附件信息';
ALTER TABLE medical_record MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE medical_record MODIFY COLUMN status INT COMMENT '状态：0-进行中, 1-完成, 2-取消';
ALTER TABLE medical_record MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- medication 字段注释
-- ============================================

ALTER TABLE medication MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE medication MODIFY COLUMN patient_id BIGINT COMMENT '患者ID';
ALTER TABLE medication MODIFY COLUMN doctor_id BIGINT COMMENT '医生ID';
ALTER TABLE medication MODIFY COLUMN medication_name VARCHAR(100) COMMENT '药品名称';
ALTER TABLE medication MODIFY COLUMN date DATETIME COMMENT '用药开始日期';
ALTER TABLE medication MODIFY COLUMN frequency VARCHAR(50) COMMENT '用药频率';
ALTER TABLE medication MODIFY COLUMN route VARCHAR(50) COMMENT '给药途径';
ALTER TABLE medication MODIFY COLUMN duration VARCHAR(50) COMMENT '服用时长：如1个月、7天';
ALTER TABLE medication MODIFY COLUMN notes TEXT COMMENT '备注';
ALTER TABLE medication MODIFY COLUMN end_date DATETIME COMMENT '用药结束日期';
ALTER TABLE medication MODIFY COLUMN status INT COMMENT '状态：0-进行中, 1-完成, 2-取消';
ALTER TABLE medication MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE medication MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- patient 字段注释
-- ============================================

ALTER TABLE patient MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE patient MODIFY COLUMN name VARCHAR(50) COMMENT '患者姓名';
ALTER TABLE patient MODIFY COLUMN gender VARCHAR(10) COMMENT '性别：男/女';
ALTER TABLE patient MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号';
ALTER TABLE patient MODIFY COLUMN avatar VARCHAR(255) COMMENT '头像URL';
ALTER TABLE patient MODIFY COLUMN id_card VARCHAR(18) COMMENT '身份证号';
ALTER TABLE patient MODIFY COLUMN is_real_auth TINYINT(1) COMMENT '是否实名认证：0-未认证, 1-已认证';
ALTER TABLE patient MODIFY COLUMN password VARCHAR(100) COMMENT '密码（加密存储）';
ALTER TABLE patient MODIFY COLUMN birth_date DATETIME COMMENT '出生日期';
ALTER TABLE patient MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE patient MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================
-- patient_disease 字段注释
-- ============================================

ALTER TABLE patient_disease MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE patient_disease MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ============================================
-- patient_doctor_relation 字段注释
-- ============================================

ALTER TABLE patient_doctor_relation MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE patient_doctor_relation MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';