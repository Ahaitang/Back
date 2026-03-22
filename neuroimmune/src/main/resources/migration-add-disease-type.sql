-- 添加 disease_type 字段到 patient 表
ALTER TABLE patient ADD COLUMN disease_type VARCHAR(50) DEFAULT NULL COMMENT '疾病分类: MS, NMOSD, MG, MOGAD, 自身免疫性脑炎, GBS, CIDP, 其它疾病' AFTER doctor_name;

-- 更新现有数据的疾病分类（示例）
UPDATE patient SET disease_type = CASE
    WHEN id = 1 THEN 'MS'
    WHEN id = 2 THEN 'NMOSD'
    WHEN id = 3 THEN 'GBS'
    WHEN id = 5 THEN 'MG'
    ELSE '其它疾病'
END;