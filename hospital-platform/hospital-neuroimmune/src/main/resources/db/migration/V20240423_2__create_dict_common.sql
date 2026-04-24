-- 创建通用字典表
CREATE TABLE IF NOT EXISTS dict_common (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    code VARCHAR(50) COMMENT '编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    description VARCHAR(255) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active INT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用字典表';

-- 初始化科室字典
INSERT INTO dict_common (dict_type, code, name, sort_order) VALUES
('department', 'NEUROLOGY', '神经内科', 1),
('department', 'NEURO_SURGERY', '神经外科', 2),
('department', 'NEUROIMMUNE', '神经免疫科', 3),
('department', 'REHAB', '康复科', 4),
('department', 'ICU', 'ICU', 5);

-- 初始化职称字典
INSERT INTO dict_common (dict_type, code, name, sort_order) VALUES
('title', 'DIRECTOR', '主任医师', 1),
('title', 'ASSOCIATE_DIRECTOR', '副主任医师', 2),
('title', 'ATTENDING', '主治医师', 3),
('title', 'RESIDENT', '住院医师', 4),
('title', 'TRAINER', '规培医师', 5);

-- 初始化病历类型字典
INSERT INTO dict_common (dict_type, code, name, sort_order) VALUES
('recordType', 'OUTPATIENT', '门诊病历', 1),
('recordType', 'INPATIENT', '住院病历', 2),
('recordType', 'EXTERNAL', '外院病历', 3);

-- 初始化随访类型字典
INSERT INTO dict_common (dict_type, code, name, sort_order) VALUES
('followUpType', 'REGULAR', '定期随访', 1),
('followUpType', 'RETURN', '复诊随访', 2),
('followUpType', 'MEDICATION', '用药随访', 3),
('followUpType', 'EVALUATION', '评估随访', 4),
('followUpType', 'URGENT', '紧急随访', 5);

-- 初始化性别字典
INSERT INTO dict_common (dict_type, code, name, sort_order) VALUES
('gender', 'MALE', '男', 1),
('gender', 'FEMALE', '女', 2);

-- 初始化状态字典（用于随访、用药、病历等）
INSERT INTO dict_common (dict_type, code, name, description, sort_order) VALUES
('status', 'IN_PROGRESS', '进行中', '正在进行的状态', 1),
('status', 'COMPLETED', '已完成', '已完成的状态', 2),
('status', 'CANCELLED', '已取消', '已取消的状态', 3);