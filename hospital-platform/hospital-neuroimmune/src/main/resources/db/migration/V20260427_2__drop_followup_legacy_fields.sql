-- V20260427_2__drop_followup_legacy_fields.sql
-- 删除 follow_up 表的废弃字段

-- 1. 删除原随访时间字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS date;

-- 2. 删除原随访项目字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS project;

-- 3. 删除原随访类型字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS type;

-- 4. 删除原内容描述字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS content;

-- 5. 删除医院字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS hospital;

-- 6. 删除科室字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS department;

-- 7. 删除原门诊时间字段
ALTER TABLE follow_up DROP COLUMN IF EXISTS outpatientTime;