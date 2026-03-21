-- ============================================
-- 若之前执行过 add_patient_creator_id.sql，可执行本脚本移除 patient.creator_id
-- 改用 patient_doctor 表（最早关联的医生为创建人）判断
-- ============================================
ALTER TABLE `patient` DROP COLUMN IF EXISTS `creator_id`;
