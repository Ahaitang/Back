-- 删除所有冗余名称字段
-- 这些字段已改为 transient，由服务层通过 JOIN 查询填充

ALTER TABLE follow_up DROP COLUMN patient_name;
ALTER TABLE follow_up DROP COLUMN patient_gender;
ALTER TABLE follow_up DROP COLUMN patient_age;
ALTER TABLE follow_up DROP COLUMN doctor_name;

ALTER TABLE medication DROP COLUMN patient_name;
ALTER TABLE medication DROP COLUMN doctor_name;

ALTER TABLE medical_record DROP COLUMN patient_name;
ALTER TABLE medical_record DROP COLUMN doctor_name;

ALTER TABLE disease_episode DROP COLUMN patient_name;

ALTER TABLE patient_doctor_relation DROP COLUMN patient_name;
ALTER TABLE patient_doctor_relation DROP COLUMN doctor_name;