-- ============================================
-- 医院医疗系统建表脚本（纯结构）
-- 包含: QMG + Neuroimmune + Session Audit
-- ============================================

-- ============================================
-- 第一部分: 创建数据库
-- ============================================

CREATE DATABASE IF NOT EXISTS QMG
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS neuroimmune
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS session_audit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ============================================
-- 第二部分: QMG 系统建表
-- ============================================

USE QMG;

CREATE TABLE IF NOT EXISTS `doctor` (
    `id` INT AUTO_INCREMENT COMMENT '医生ID' PRIMARY KEY,
    `employee_number` VARCHAR(50) NOT NULL COMMENT '工号（唯一标识）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `level` INT DEFAULT 2 NOT NULL COMMENT '权限等级：0=超级管理员，1=管理员，2=普通医生',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    `role` VARCHAR(50) DEFAULT 'doctor' COMMENT '角色',
    UNIQUE KEY `uk_employee_number` (`employee_number`),
    INDEX `idx_employee_number` (`employee_number`),
    INDEX `idx_username` (`username`)
) COMMENT '医生表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `patient` (
    `id` INT AUTO_INCREMENT COMMENT '患者ID' PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) NOT NULL COMMENT '性别：male/female',
    `admission_number` VARCHAR(50) NOT NULL COMMENT '住院号',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    UNIQUE KEY `uk_admission_number` (`admission_number`),
    INDEX `idx_admission_number` (`admission_number`),
    INDEX `idx_name` (`name`)
) COMMENT '患者信息表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `patient_doctor` (
    `id` INT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` INT NOT NULL COMMENT '患者ID',
    `doctor_id` INT NOT NULL COMMENT '医生ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_patient_doctor` (`patient_id`, `doctor_id`),
    INDEX `idx_doctor_id` (`doctor_id`),
    INDEX `idx_patient_id` (`patient_id`),
    INDEX `idx_pd_patient_doctor` (`patient_id`, `doctor_id`),
    CONSTRAINT `fk_patient_doctor_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_patient_doctor_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE
) COMMENT '患者-医生对应表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `questionnaire_item` (
    `id` INT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `key` VARCHAR(50) NOT NULL COMMENT '项目键名（唯一标识）',
    `category` VARCHAR(50) NOT NULL COMMENT '分类：eyes/bulbar/respiratory/limbs',
    `display_order` INT DEFAULT 0 NOT NULL COMMENT '显示顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_key` (`key`),
    INDEX `idx_category` (`category`),
    INDEX `idx_display_order` (`display_order`)
) COMMENT '问卷项目配置表' CHARSET utf8mb4;

CREATE TABLE IF NOT EXISTS `questionnaire_option` (
    `id` INT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `item_key` VARCHAR(50) NOT NULL COMMENT '项目键名',
    `label` VARCHAR(200) NOT NULL COMMENT '选项标签',
    `value` VARCHAR(50) NOT NULL COMMENT '选项值',
    `score` INT DEFAULT 0 NOT NULL COMMENT '得分',
    `display_order` INT DEFAULT 0 NOT NULL COMMENT '显示顺序',
    `allow_custom_input` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否允许用户输入自定义数据（0=不允许，1=允许）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_display_order` (`display_order`),
    INDEX `idx_item_key` (`item_key`),
    CONSTRAINT `fk_option_item` FOREIGN KEY (`item_key`) REFERENCES `questionnaire_item` (`key`) ON DELETE CASCADE
) COMMENT '问卷选项配置表' CHARSET utf8mb4;

CREATE TABLE IF NOT EXISTS `questionnaire_record` (
    `id` INT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` INT NOT NULL COMMENT '患者ID',
    `admission_number` VARCHAR(50) NOT NULL COMMENT '住院号（冗余字段，便于查询）',
    `assessment_date` DATE NOT NULL COMMENT '测评日期',
    `selections` TEXT NOT NULL COMMENT '选择的选项（JSON格式）',
    `item_scores` TEXT NOT NULL COMMENT '各项得分（JSON格式）',
    `total_score` INT DEFAULT 0 NOT NULL COMMENT '总分',
    `category_scores` TEXT NOT NULL COMMENT '分类得分（JSON格式）',
    `doctor_id` INT COMMENT '医生ID（创建记录的医生）',
    `doctor_username` VARCHAR(50) COMMENT '医生用户名（冗余字段）',
    `modified_by` VARCHAR(50) COMMENT '最后修改人员（用户名）',
    `user_input_data` TEXT COMMENT '用户自定义输入数据（JSON格式，如备注、说明等）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    INDEX `idx_admission_date` (`admission_number`, `assessment_date`),
    INDEX `idx_admission_number` (`admission_number`),
    INDEX `idx_assessment_date` (`assessment_date`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_doctor_id` (`doctor_id`),
    INDEX `idx_modified_by` (`modified_by`),
    INDEX `idx_patient_date` (`patient_id`, `assessment_date`),
    INDEX `idx_patient_id` (`patient_id`),
    CONSTRAINT `fk_questionnaire_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_questionnaire_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE
) COMMENT '问卷结果表' CHARSET utf8mb4;

-- ============================================
-- 第三部分: Neuroimmune 系统建表
-- ============================================

USE neuroimmune;

CREATE TABLE IF NOT EXISTS `dict_common` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `code` VARCHAR(50) COMMENT '编码',
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `description` VARCHAR(255) COMMENT '描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_active` INT DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    INDEX `idx_dict_type` (`dict_type`)
) COMMENT '通用字典表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `disease_episode` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT COMMENT '患者ID',
    `episode_number` INT COMMENT '发作次数',
    `episode_date` DATETIME COMMENT '发作时间',
    `chief_complaint` VARCHAR(500) COMMENT '主诉',
    `symptoms` VARCHAR(1000) COMMENT '症状',
    `disease_progress` TEXT COMMENT '病情变化过程',
    `treatment_process` TEXT COMMENT '诊治经过',
    `diagnosis` VARCHAR(500) COMMENT '诊断结果',
    `hospital` VARCHAR(200) COMMENT '就诊医院',
    `department` VARCHAR(100) COMMENT '科室',
    `notes` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    INDEX `idx_de_is_deleted` (`is_deleted`),
    INDEX `idx_de_patient_date` (`patient_id`, `episode_date`),
    INDEX `idx_patient_id` (`patient_id`)
) COMMENT '疾病发作记录' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `doctor` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `name` VARCHAR(50) COMMENT '医生姓名',
    `title` VARCHAR(50) COMMENT '职称',
    `department` VARCHAR(50) COMMENT '科室',
    `hospital` VARCHAR(100) COMMENT '所属医院',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `password` VARCHAR(100) COMMENT '密码（加密存储）',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    `level` INT COMMENT '管理等级，1最高，null表示普通医生',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_doctor_phone` (`phone`)
) COMMENT '医生信息表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `doctor_role` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `is_active` INT DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    UNIQUE KEY `uk_doctor_role` (`doctor_id`, `role_code`),
    INDEX `idx_doctor_id` (`doctor_id`),
    INDEX `idx_role_code` (`role_code`)
) COMMENT '医生角色关联表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `follow_up` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT COMMENT '患者ID',
    `doctor_id` BIGINT COMMENT '医生ID',
    `date` DATETIME COMMENT '随访日期',
    `project` VARCHAR(100) COMMENT '随访项目',
    `type` VARCHAR(50) COMMENT '随访类型',
    `content` MEDIUMTEXT COMMENT '随访内容',
    `outpatient_time` DATETIME COMMENT '门诊时间',
    `hospitalization_time` DATETIME COMMENT '住院时间',
    `examination_items` MEDIUMTEXT COMMENT '检查项目',
    `hospital` VARCHAR(100) COMMENT '医院',
    `department` VARCHAR(50) COMMENT '科室',
    `notes` MEDIUMTEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` INT DEFAULT 0 COMMENT '0-进行中, 1-完成, 2-取消',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    `outpatient_cycle_type` VARCHAR(20) COMMENT '周期类型: monthly/weekly/quarterly',
    `outpatient_cycle_value` VARCHAR(50) COMMENT '周期值: 每月几号/每周几/季度日期',
    `outpatient_time_slot` VARCHAR(20) COMMENT '时间段: morning/afternoon/evening',
    `follow_up_exam_type_id` BIGINT COMMENT '随访检查类型字典ID',
    INDEX `idx_fu_doctor_status_date` (`doctor_id`, `date`),
    INDEX `idx_fu_is_deleted` (`is_deleted`),
    INDEX `idx_fu_patient_date` (`patient_id`, `date`)
) COMMENT '随访记录表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `medical_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT COMMENT '患者ID',
    `doctor_id` BIGINT COMMENT '医生ID',
    `type` VARCHAR(50) COMMENT '病历类型：门诊病历、住院病历、外院病历',
    `diagnosis` VARCHAR(200) COMMENT '诊断结果',
    `hospital` VARCHAR(100) COMMENT '就诊医院',
    `department` VARCHAR(50) COMMENT '科室',
    `date` DATETIME COMMENT '病历日期',
    `content` MEDIUMTEXT COMMENT '病历内容',
    `attachments` MEDIUMTEXT COMMENT '附件信息',
    `notes` VARCHAR(500) COMMENT '备注',
    `related_episode_id` BIGINT COMMENT '关联的发作记录ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` INT COMMENT '状态：0-进行中, 1-完成, 2-取消',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    INDEX `idx_mr_doctor` (`doctor_id`),
    INDEX `idx_mr_is_deleted` (`is_deleted`),
    INDEX `idx_mr_patient_date` (`patient_id`, `date`),
    INDEX `idx_mr_related_episode` (`related_episode_id`)
) COMMENT '病历记录表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `medication` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT COMMENT '患者ID',
    `doctor_id` BIGINT COMMENT '医生ID',
    `medication_name` VARCHAR(100) COMMENT '药品名称',
    `date` DATETIME COMMENT '用药开始日期',
    `frequency` VARCHAR(50) COMMENT '用药频率',
    `route` VARCHAR(50) COMMENT '给药途径',
    `duration` VARCHAR(50) COMMENT '服用时长：如1个月、7天',
    `notes` MEDIUMTEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `end_date` DATETIME COMMENT '用药结束日期',
    `status` INT COMMENT '状态：0-进行中, 1-完成, 2-取消',
    `dosage_value` DECIMAL(10, 2) COMMENT '剂量数值',
    `dosage_unit` VARCHAR(20) COMMENT '剂量单位',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    INDEX `idx_med_doctor` (`doctor_id`),
    INDEX `idx_med_is_deleted` (`is_deleted`),
    INDEX `idx_med_patient_date` (`patient_id`, `date`)
) COMMENT '用药记录表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `patient` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `name` VARCHAR(50) COMMENT '患者姓名',
    `gender` VARCHAR(10) COMMENT '性别：男/女',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `id_card` VARCHAR(18) COMMENT '身份证号',
    `is_real_auth` TINYINT(1) COMMENT '是否实名认证：0-未认证, 1-已认证',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `password` VARCHAR(100) COMMENT '密码（加密存储）',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    `birth_date` DATETIME COMMENT '出生日期',
    `status` INT DEFAULT 1 COMMENT '患者状态: 0-待确认, 1-正常, 2-已拒绝, 3-禁用',
    INDEX `idx_patient_is_deleted` (`is_deleted`),
    INDEX `idx_patient_phone` (`phone`)
) COMMENT '患者信息表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `patient_disease` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `disease_code` VARCHAR(20) NOT NULL COMMENT '疾病编码: MS, NMOSD, MG, MOGAD, AUTO_ENCEPHALITIS, GBS, CIDP, OTHER',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_patient_disease` (`patient_id`, `disease_code`),
    INDEX `idx_disease_code` (`disease_code`),
    INDEX `idx_patient_id` (`patient_id`)
) COMMENT '患者疾病关联表' COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `patient_doctor_relation` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    `patient_id` BIGINT NOT NULL COMMENT '患者ID',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `relation_type` VARCHAR(20) DEFAULT 'primary' COMMENT '关系类型：primary-主治医生, consultant-会诊医生',
    `bind_method` VARCHAR(20) DEFAULT 'patient' COMMENT '绑定方式：system-系统分配, patient-患者选择, doctor-医生邀请',
    `remark` VARCHAR(255) COMMENT '备注',
    `bind_time` DATETIME COMMENT '绑定时间',
    `unbind_time` DATETIME COMMENT '解绑时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '0-有效, 1-无效',
    `status` TINYINT DEFAULT 1 COMMENT '绑定状态：1-生效中, 0-已解绑',
    `bind_status` INT DEFAULT 1 COMMENT '绑定状态: 0-待确认, 1-已确认, 2-已拒绝',
    `request_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定请求时间',
    `confirm_time` DATETIME COMMENT '确认/拒绝时间',
    INDEX `idx_doctor_id` (`doctor_id`),
    INDEX `idx_patient_id` (`patient_id`),
    INDEX `idx_pdr_doctor_status` (`doctor_id`),
    INDEX `idx_pdr_is_deleted` (`is_deleted`),
    INDEX `idx_pdr_patient_status` (`patient_id`)
) COMMENT '患者-医生绑定关系表' COLLATE utf8mb4_unicode_ci;

-- ============================================
-- 第四部分: Session Audit 建表
-- ============================================

USE session_audit;

CREATE TABLE IF NOT EXISTS `black_list` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色',
    `module` VARCHAR(20) NOT NULL COMMENT '模块',
    `reason` VARCHAR(255) COMMENT '封禁原因',
    `ban_time` DATETIME NOT NULL COMMENT '封禁时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/released',
    `operator_id` BIGINT COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME,
    INDEX `idx_expire` (`expire_time`),
    INDEX `idx_status` (`status`),
    INDEX `idx_user` (`user_id`, `role`, `module`)
) COMMENT '黑名单';

CREATE TABLE IF NOT EXISTS `session_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：doctor/admin/patient',
    `module` VARCHAR(20) NOT NULL COMMENT '模块：qmg/neuroimmune',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `logout_time` DATETIME COMMENT '登出时间',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(255) COMMENT '浏览器User-Agent',
    `device` VARCHAR(100) COMMENT '解析后的设备信息',
    `duration_seconds` INT COMMENT '会话时长（秒）',
    `operation_type` VARCHAR(20) COMMENT '操作类型：LOGIN/LOGOUT/KICK_OFFLINE',
    `operator_id` BIGINT COMMENT '踢下线操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_login_time` (`login_time`),
    INDEX `idx_operation` (`operation_type`),
    INDEX `idx_user` (`user_id`, `role`, `module`)
) COMMENT '会话审计日志';

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(255) NOT NULL COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '配置说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME,
    UNIQUE KEY `uk_config_key` (`config_key`)
) COMMENT '系统配置';

-- ============================================
-- 完成
-- ============================================
SELECT '建表完成！' AS status;