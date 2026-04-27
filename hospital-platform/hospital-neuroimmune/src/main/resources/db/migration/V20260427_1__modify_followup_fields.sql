-- 随访计划字段简化迁移
-- 新增周期性门诊随访字段
ALTER TABLE follow_up ADD COLUMN outpatient_cycle_type VARCHAR(20) COMMENT '周期类型: monthly/weekly/quarterly';
ALTER TABLE follow_up ADD COLUMN outpatient_cycle_value VARCHAR(50) COMMENT '周期值: 每月几号/每周几/季度日期';
ALTER TABLE follow_up ADD COLUMN outpatient_time_slot VARCHAR(20) COMMENT '时间段: morning/afternoon/evening';

-- 新增随访检查类型关联
ALTER TABLE follow_up ADD COLUMN follow_up_exam_type_id BIGINT COMMENT '随访检查类型字典ID';

-- 保留原字段用于历史数据兼容，新增字段允许为空
-- examinationItems, hospitalizationTime, notes 字段已存在，无需修改

-- 新增字典类型 followUpExamType（随访检查类型）
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active) VALUES
('followUpExamType', 'IMMUNOSUPPRESSANT', '免疫抑制剂随访', '["血常规","肝肾功能","电解质"]', 1, 1),
('followUpExamType', 'BIOLOGIC', '生物制剂随访', '["淋巴细胞亚群","TSPOT"]', 2, 1),
('followUpExamType', 'ROUTINE', '常规随访', '["血常规","尿常规"]', 3, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);