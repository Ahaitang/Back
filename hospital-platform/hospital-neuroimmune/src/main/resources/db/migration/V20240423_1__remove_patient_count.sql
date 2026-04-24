-- 删除 doctor 表中无效的 patient_count 字段
-- 该字段值不会被同步更新，每次查询时动态计算即可

ALTER TABLE doctor DROP COLUMN IF EXISTS patient_count;