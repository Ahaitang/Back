-- 删除 patient.has_follow_up 字段
-- 该字段改为从 follow_up 表计算

ALTER TABLE patient DROP COLUMN has_follow_up;