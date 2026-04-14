-- 添加 notes 字段到 medical_record 表
ALTER TABLE medical_record ADD COLUMN notes VARCHAR(500) COMMENT '备注' AFTER attachments;