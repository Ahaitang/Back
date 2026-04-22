-- ============================================
-- 03. Neuroimmune 系统数据库结构
-- 神经免疫疾病随访系统
-- 执行顺序: 第四个执行（在 02-qmg-data.sql 之后）
-- ============================================

USE neuroimmune;

-- ============================================
-- 管理员表
-- ============================================
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50),
    level INT DEFAULT 1 COMMENT '管理员等级：1-超级管理员 2-普通管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 医生表
-- ============================================
CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(50),
    department VARCHAR(50),
    hospital VARCHAR(100),
    phone VARCHAR(20),
    password VARCHAR(100),
    avatar VARCHAR(255),
    patient_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 患者表
-- ============================================
CREATE TABLE IF NOT EXISTS patient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    age INT,
    phone VARCHAR(20),
    password VARCHAR(100),
    avatar VARCHAR(255),
    id_card VARCHAR(18),
    has_follow_up TINYINT(1) DEFAULT 0,
    is_real_auth TINYINT(1) DEFAULT 0,
    doctor_id BIGINT,
    doctor_name VARCHAR(50),
    disease_type VARCHAR(50) DEFAULT NULL COMMENT '疾病分类: MS, NMOSD, MG, MOGAD, 自身免疫性脑炎, GBS, CIDP, 其它疾病',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- 随访记录表
-- ============================================
CREATE TABLE IF NOT EXISTS follow_up (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(50),
    patient_gender VARCHAR(10),
    patient_age INT,
    doctor_id BIGINT,
    doctor_name VARCHAR(50),
    date DATE,
    project VARCHAR(100),
    type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    status_text VARCHAR(20) DEFAULT '待随访',
    content TEXT,
    outpatient_time DATETIME DEFAULT NULL COMMENT '门诊时间',
    hospitalization_time DATE DEFAULT NULL COMMENT '住院时间',
    examination_items TEXT DEFAULT NULL COMMENT '检查项目',
    hospital VARCHAR(100) DEFAULT NULL COMMENT '医院',
    department VARCHAR(50) DEFAULT NULL COMMENT '科室',
    notes TEXT DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 用药记录表
-- ============================================
CREATE TABLE IF NOT EXISTS medication (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(50),
    doctor_id BIGINT,
    doctor_name VARCHAR(50),
    medication_name VARCHAR(100) NOT NULL,
    date DATE,
    dosage VARCHAR(50),
    unit VARCHAR(20),
    frequency VARCHAR(50),
    route VARCHAR(50),
    duration VARCHAR(50),
    notes TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 病历记录表
-- ============================================
CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(50),
    type VARCHAR(50),
    diagnosis VARCHAR(200),
    hospital VARCHAR(100),
    department VARCHAR(50),
    doctor_name VARCHAR(50),
    date DATE,
    content TEXT,
    attachments TEXT,
    notes VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 疾病发作记录表
-- ============================================
CREATE TABLE IF NOT EXISTS disease_episode (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT DEFAULT NULL,
    patient_name VARCHAR(100) DEFAULT NULL,
    episode_number INT DEFAULT NULL COMMENT '发作次数',
    episode_date DATE DEFAULT NULL COMMENT '发作时间',
    chief_complaint VARCHAR(500) DEFAULT NULL COMMENT '主诉',
    symptoms VARCHAR(1000) DEFAULT NULL COMMENT '症状',
    disease_progress TEXT COMMENT '病情变化过程',
    treatment_process TEXT COMMENT '诊治经过',
    diagnosis VARCHAR(500) DEFAULT NULL COMMENT '诊断结果',
    hospital VARCHAR(200) DEFAULT NULL COMMENT '就诊医院',
    department VARCHAR(100) DEFAULT NULL COMMENT '科室',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_patient_id (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疾病发作记录';

-- ============================================
-- 患者-医生绑定关系表
-- ============================================
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

-- ============================================
-- 性能优化索引（如果已存在会报错，可忽略）
-- ============================================
-- follow_up 表优化索引
CREATE INDEX IF NOT EXISTS idx_fu_doctor_status_date ON follow_up(doctor_id, status, date);
CREATE INDEX IF NOT EXISTS idx_fu_patient_date ON follow_up(patient_id, date);
CREATE INDEX IF NOT EXISTS idx_fu_status ON follow_up(status);

-- patient 表优化索引
CREATE INDEX IF NOT EXISTS idx_patient_doctor_update ON patient(doctor_id, update_time);
CREATE INDEX IF NOT EXISTS idx_patient_phone ON patient(phone);

-- patient_doctor_relation 表优化索引
CREATE INDEX IF NOT EXISTS idx_pdr_patient_status ON patient_doctor_relation(patient_id, status);
CREATE INDEX IF NOT EXISTS idx_pdr_doctor_status ON patient_doctor_relation(doctor_id, status);

-- medication 表优化索引
CREATE INDEX IF NOT EXISTS idx_med_patient_date ON medication(patient_id, date);
CREATE INDEX IF NOT EXISTS idx_med_doctor ON medication(doctor_id);

-- ============================================
-- 示例数据（可选，生产环境可删除）
-- ============================================

-- 示例医生数据（密码为 123456 的BCrypt加密值）
INSERT INTO doctor (name, title, department, hospital, phone, password) VALUES
('张医生', '主任医师', '神经内科', 'XX医院', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'),
('李医生', '副主任医师', '神经内科', 'XX医院', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH'),
('王医生', '主治医师', '神经免疫科', 'XX医院', '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH');

-- 示例患者数据（密码为 123456 的BCrypt加密值）
INSERT INTO patient (name, gender, age, phone, password, has_follow_up, is_real_auth, doctor_id, doctor_name, disease_type) VALUES
('刘博超', '男', 45, '18344029312', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 1, 1, '张医生', 'MS'),
('张哲瀚', '男', 45, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1, 1, '张医生', 'NMOSD'),
('王某某', '女', 38, '13900139000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 0, 2, '李医生', 'GBS'),
('李明', '男', 52, '15800158000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1, 1, '张医生', 'MG'),
('赵芳', '女', 41, '18600186000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 1, 2, '李医生', 'MG'),
('陈建国', '男', 58, '17700177000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1, 1, '张医生', 'MS'),
('孙丽华', '女', 35, '13500135000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 0, 2, '李医生', 'CIDP'),
('周强', '男', 48, '15000150000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1, 1, '张医生', 'NMOSD');

-- 示例随访数据
INSERT INTO follow_up (patient_id, patient_name, patient_gender, patient_age, doctor_id, doctor_name, date, project, type, status, status_text) VALUES
(1, '刘博超', '男', 45, 1, '张医生', '2025-02-28', '神经功能评估', '定期随访', 'pending', '待随访'),
(2, '张哲瀚', '男', 45, 1, '张医生', '2025-02-27', '复诊', '复诊随访', 'pending', '待随访'),
(3, '王某某', '女', 38, 2, '李医生', '2025-02-25', '用药复查', '用药随访', 'completed', '已完成'),
(5, '赵芳', '女', 41, 2, '李医生', '2025-02-28', '量表评估', '评估随访', 'pending', '待随访'),
(7, '孙丽华', '女', 35, 2, '李医生', '2025-02-26', '病情跟踪', '定期随访', 'completed', '已完成');

-- 示例用药数据
INSERT INTO medication (patient_id, patient_name, doctor_id, doctor_name, medication_name, date, dosage, unit, frequency, route, duration, notes) VALUES
(1, '刘博超', 1, '张医生', '甲钴胺片', '2025-02-20', '0.5', 'mg/次', '一日三次', '口服', '30天', '饭后服用'),
(1, '刘博超', 1, '张医生', '维生素B1', '2025-02-15', '10', 'mg/次', '一日两次', '口服', '30天', NULL),
(2, '张哲瀚', 1, '张医生', '泼尼松片', '2025-02-18', '5', 'mg/次', '一日一次', '口服', '14天', '早晨顿服'),
(3, '王某某', 2, '李医生', '丙戊酸钠', '2025-02-22', '200', 'mg/次', '一日两次', '口服', '长期', NULL),
(5, '赵芳', 2, '李医生', '阿司匹林肠溶片', '2025-02-25', '100', 'mg/次', '一日一次', '口服', '长期', '餐后服用');

-- 示例病历数据
INSERT INTO medical_record (patient_id, patient_name, type, diagnosis, hospital, department, doctor_name, date, content) VALUES
(1, '刘博超', '门诊病历', '多发性硬化', 'XX医院', '神经内科', '张医生', '2025-02-15', '患者主诉视力模糊，肢体麻木。检查显示脑白质多发脱髓鞘病变。'),
(2, '张哲瀚', '住院病历', '视神经脊髓炎', 'XX医院', '神经内科', '张医生', '2025-01-20', '双眼视力下降，伴有下肢无力。脊髓MRI显示颈段长节段病变。'),
(3, '王某某', '门诊病历', '格林-巴利综合征', 'XX医院', '神经内科', '李医生', '2025-02-10', '四肢进行性无力，腱反射消失。脑脊液蛋白-细胞分离。'),
(5, '赵芳', '外院病历', '重症肌无力', '外院', '神经内科', '外院医生', '2025-01-05', '眼睑下垂，复视。新斯的明试验阳性。');

-- 从现有患者数据初始化绑定关系
INSERT INTO patient_doctor_relation (patient_id, patient_name, doctor_id, doctor_name, relation_type, status, bind_method, bind_time, create_time)
SELECT id, name, doctor_id, doctor_name, 'primary', 'active', 'system', create_time, create_time
FROM patient
WHERE doctor_id IS NOT NULL AND doctor_id > 0;

-- 提示
SELECT 'Neuroimmune 数据库结构创建完成！' AS status;