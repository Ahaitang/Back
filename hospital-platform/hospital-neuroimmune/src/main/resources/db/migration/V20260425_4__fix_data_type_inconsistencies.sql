-- 修复数据类型不一致

-- medication.end_date: DATETIME -> DATE（与其他日期字段统一）
ALTER TABLE medication MODIFY COLUMN end_date DATE DEFAULT NULL COMMENT '结束日期';

-- medication: 删除冗余的 unit 列（已被 dosage_unit 取代）
ALTER TABLE medication DROP COLUMN unit;
