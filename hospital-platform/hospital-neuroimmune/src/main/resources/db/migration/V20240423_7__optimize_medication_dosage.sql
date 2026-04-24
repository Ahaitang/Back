-- 添加剂量数值和单位字段
ALTER TABLE medication ADD COLUMN dosage_value DECIMAL(10,2) COMMENT '剂量数值';
ALTER TABLE medication ADD COLUMN dosage_unit VARCHAR(20) COMMENT '剂量单位';