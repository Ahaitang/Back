-- ============================================
-- neuroimmune 数据库完整迁移脚本
-- 执行方式: mysql -u root -p neuroimmune < migration-all.sql
-- ============================================

-- 1. patient 表 - 添加 disease_type 字段
ALTER TABLE patient ADD COLUMN IF NOT EXISTS disease_type VARCHAR(50) DEFAULT NULL
    COMMENT '疾病分类: MS, NMOSD, MG, MOGAD, 自身免疫性脑炎, GBS, CIDP, 其它疾病' AFTER doctor_name;

-- 2. follow_up 表 - 添加缺失的字段
ALTER TABLE follow_up
    ADD COLUMN IF NOT EXISTS outpatient_time DATETIME DEFAULT NULL COMMENT '门诊时间' AFTER content,
    ADD COLUMN IF NOT EXISTS hospitalization_time DATE DEFAULT NULL COMMENT '住院时间' AFTER outpatient_time,
    ADD COLUMN IF NOT EXISTS examination_items TEXT DEFAULT NULL COMMENT '检查项目' AFTER hospitalization_time,
    ADD COLUMN IF NOT EXISTS hospital VARCHAR(100) DEFAULT NULL COMMENT '医院' AFTER examination_items,
    ADD COLUMN IF NOT EXISTS department VARCHAR(50) DEFAULT NULL COMMENT '科室' AFTER hospital,
    ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT NULL COMMENT '备注' AFTER department;

-- 3. 如果上面语句报错，使用以下替代方案（MySQL 5.7 不支持 IF NOT EXISTS）
-- 请逐条执行，忽略已存在的列错误

-- ALTER TABLE patient ADD COLUMN disease_type VARCHAR(50) DEFAULT NULL COMMENT '疾病分类' AFTER doctor_name;

-- ALTER TABLE follow_up ADD COLUMN outpatient_time DATETIME DEFAULT NULL COMMENT '门诊时间' AFTER content;
-- ALTER TABLE follow_up ADD COLUMN hospitalization_time DATE DEFAULT NULL COMMENT '住院时间' AFTER outpatient_time;
-- ALTER TABLE follow_up ADD COLUMN examination_items TEXT DEFAULT NULL COMMENT '检查项目' AFTER hospitalization_time;
-- ALTER TABLE follow_up ADD COLUMN hospital VARCHAR(100) DEFAULT NULL COMMENT '医院' AFTER examination_items;
-- ALTER TABLE follow_up ADD COLUMN department VARCHAR(50) DEFAULT NULL COMMENT '科室' AFTER hospital;
-- ALTER TABLE follow_up ADD COLUMN notes TEXT DEFAULT NULL COMMENT '备注' AFTER department;

-- 4. 更新患者疾病分类示例数据
UPDATE patient SET disease_type = CASE
    WHEN id = 1 THEN 'MS'
    WHEN id = 2 THEN 'NMOSD'
    WHEN id = 3 THEN 'GBS'
    WHEN id = 4 THEN 'MG'
    WHEN id = 5 THEN 'MG'
    WHEN id = 6 THEN 'MS'
    WHEN id = 7 THEN 'CIDP'
    WHEN id = 8 THEN 'NMOSD'
    ELSE '其它疾病'
END WHERE disease_type IS NULL;

-- 完成
SELECT 'Migration completed successfully!' AS status;