-- 添加 follow_up 表缺失的字段
ALTER TABLE follow_up
    ADD COLUMN outpatient_time VARCHAR(50) DEFAULT NULL COMMENT '门诊时间' AFTER content,
    ADD COLUMN hospitalization_time VARCHAR(50) DEFAULT NULL COMMENT '住院时间' AFTER outpatient_time,
    ADD COLUMN examination_items TEXT DEFAULT NULL COMMENT '检查项目' AFTER hospitalization_time,
    ADD COLUMN hospital VARCHAR(100) DEFAULT NULL COMMENT '医院' AFTER examination_items,
    ADD COLUMN department VARCHAR(50) DEFAULT NULL COMMENT '科室' AFTER hospital,
    ADD COLUMN notes TEXT DEFAULT NULL COMMENT '备注' AFTER department;

-- 添加 medication 表缺失的字段（如果需要）
-- ALTER TABLE medication ADD COLUMN doctor_id BIGINT DEFAULT NULL AFTER patient_name;
-- ALTER TABLE medication ADD COLUMN doctor_name VARCHAR(50) DEFAULT NULL AFTER doctor_id;