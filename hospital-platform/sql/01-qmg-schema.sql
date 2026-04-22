-- ============================================
-- 01. QMG 系统数据库结构
-- 重症肌无力定量评分系统
-- 执行顺序: 第二个执行（在 00-init-databases.sql 之后）
-- ============================================

USE QMG;

-- ============================================
-- 患者表
-- ============================================
CREATE TABLE IF NOT EXISTS `patient` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '患者ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) NOT NULL COMMENT '性别：male/female',
    `admission_number` VARCHAR(50) NOT NULL UNIQUE COMMENT '住院号',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_admission_number` (`admission_number`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表';

-- ============================================
-- 医生表
-- ============================================
CREATE TABLE IF NOT EXISTS `doctor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    `employee_number` VARCHAR(50) NOT NULL UNIQUE COMMENT '工号（唯一标识）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `level` INT NOT NULL DEFAULT 2 COMMENT '权限等级：0=超级管理员，1=管理员，2=普通医生',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_employee_number` (`employee_number`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- ============================================
-- 患者-医生对应表
-- ============================================
CREATE TABLE IF NOT EXISTS `patient_doctor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `patient_id` INT NOT NULL COMMENT '患者ID',
    `doctor_id` INT NOT NULL COMMENT '医生ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_patient_doctor` (`patient_id`, `doctor_id`),
    INDEX `idx_patient_id` (`patient_id`),
    INDEX `idx_doctor_id` (`doctor_id`),
    CONSTRAINT `fk_patient_doctor_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_patient_doctor_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者-医生对应表';

-- ============================================
-- 问卷项目配置表
-- ============================================
CREATE TABLE IF NOT EXISTS `questionnaire_item` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `key` VARCHAR(50) NOT NULL UNIQUE COMMENT '项目键名（唯一标识）',
    `category` VARCHAR(50) NOT NULL COMMENT '分类：eyes/bulbar/respiratory/limbs',
    `display_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_display_order` (`display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷项目配置表';

-- ============================================
-- 问卷选项配置表
-- ============================================
CREATE TABLE IF NOT EXISTS `questionnaire_option` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `item_key` VARCHAR(50) NOT NULL COMMENT '项目键名',
    `label` VARCHAR(200) NOT NULL COMMENT '选项标签',
    `value` VARCHAR(50) NOT NULL COMMENT '选项值',
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `display_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `allow_custom_input` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许用户输入自定义数据（0=不允许，1=允许）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_item_key` (`item_key`),
    INDEX `idx_display_order` (`display_order`),
    CONSTRAINT `fk_option_item` FOREIGN KEY (`item_key`) REFERENCES `questionnaire_item` (`key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷选项配置表';

-- ============================================
-- 问卷结果表
-- ============================================
CREATE TABLE IF NOT EXISTS `questionnaire_record` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `patient_id` INT(11) NOT NULL COMMENT '患者ID',
    `admission_number` VARCHAR(50) NOT NULL COMMENT '住院号（冗余字段，便于查询）',
    `assessment_date` DATE NOT NULL COMMENT '测评日期',
    `selections` TEXT NOT NULL COMMENT '选择的选项（JSON格式）',
    `item_scores` TEXT NOT NULL COMMENT '各项得分（JSON格式）',
    `total_score` INT(11) NOT NULL DEFAULT 0 COMMENT '总分',
    `category_scores` TEXT NOT NULL COMMENT '分类得分（JSON格式）',
    `doctor_id` INT(11) DEFAULT NULL COMMENT '医生ID（创建记录的医生）',
    `doctor_username` VARCHAR(50) DEFAULT NULL COMMENT '医生用户名（冗余字段）',
    `modified_by` VARCHAR(50) DEFAULT NULL COMMENT '最后修改人员（用户名）',
    `user_input_data` TEXT DEFAULT NULL COMMENT '用户自定义输入数据（JSON格式，如备注、说明等）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_admission_number` (`admission_number`),
    KEY `idx_assessment_date` (`assessment_date`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_modified_by` (`modified_by`),
    KEY `idx_patient_date` (`patient_id`, `assessment_date`),
    KEY `idx_admission_date` (`admission_number`, `assessment_date`),
    CONSTRAINT `fk_questionnaire_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_questionnaire_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷结果表';

-- ============================================
-- 性能优化索引（如果已存在会报错，可忽略）
-- ============================================
-- questionnaire_record 表优化索引
CREATE INDEX IF NOT EXISTS idx_qr_patient_date ON questionnaire_record(patient_id, assessment_date);
CREATE INDEX IF NOT EXISTS idx_qr_doctor_create ON questionnaire_record(doctor_id, create_time);
CREATE INDEX IF NOT EXISTS idx_qr_admission_date ON questionnaire_record(admission_number, assessment_date);

-- patient_doctor 表优化索引
CREATE INDEX IF NOT EXISTS idx_pd_patient_doctor ON patient_doctor(patient_id, doctor_id);

-- 提示
SELECT 'QMG 数据库结构创建完成！' AS status;