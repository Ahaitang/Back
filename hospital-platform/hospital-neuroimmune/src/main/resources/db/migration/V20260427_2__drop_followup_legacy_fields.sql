-- V20260427_2__drop_followup_legacy_fields.sql
-- 删除 follow_up 表的废弃字段（数据已迁移至新字段）
-- 警告: 此操作将永久删除数据，执行前请确保已完成数据迁移

ALTER TABLE follow_up
  DROP COLUMN IF EXISTS date,
  DROP COLUMN IF EXISTS project,
  DROP COLUMN IF EXISTS type,
  DROP COLUMN IF EXISTS content,
  DROP COLUMN IF EXISTS hospital,
  DROP COLUMN IF EXISTS department,
  DROP COLUMN IF EXISTS outpatientTime;