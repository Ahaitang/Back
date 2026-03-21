-- 随访计划详细字段迁移脚本
-- 执行此脚本前请备份数据库

-- 添加随访计划详细字段
ALTER TABLE follow_up
ADD COLUMN outpatient_time DATETIME COMMENT '门诊时间' AFTER content,
ADD COLUMN hospitalization_time DATE COMMENT '住院时间' AFTER outpatient_time,
ADD COLUMN examination_items TEXT COMMENT '检查项目' AFTER hospitalization_time,
ADD COLUMN hospital VARCHAR(100) COMMENT '医院' AFTER examination_items,
ADD COLUMN department VARCHAR(50) COMMENT '科室' AFTER hospital,
ADD COLUMN notes TEXT COMMENT '备注' AFTER department;

-- 添加患者疾病分类字段
ALTER TABLE patient
ADD COLUMN disease_type VARCHAR(50) COMMENT '疾病分类：MS, NMOSD, MG, MOGAD, 自身免疫性脑炎, GBS, CIDP, 其它疾病' AFTER doctor_name;

-- 更新示例数据的疾病分类
UPDATE patient SET disease_type = 'MS' WHERE name = '刘博超';
UPDATE patient SET disease_type = 'NMOSD' WHERE name = '张哲瀚';
UPDATE patient SET disease_type = 'GBS' WHERE name = '王某某';
UPDATE patient SET disease_type = 'MG' WHERE name = '赵芳';
UPDATE patient SET disease_type = 'CIDP' WHERE name = '陈建国';
UPDATE patient SET disease_type = '自身免疫性脑炎' WHERE name = '孙丽华';
UPDATE patient SET disease_type = 'MS' WHERE name = '周强';