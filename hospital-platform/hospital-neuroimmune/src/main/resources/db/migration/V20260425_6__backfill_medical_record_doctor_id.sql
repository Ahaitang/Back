-- 回填 medical_record.doctor_id
-- 通过 doctor_name 匹配 doctor.id（未匹配的保留 NULL，代表外院记录）

UPDATE medical_record mr
INNER JOIN doctor d ON mr.doctor_name = d.name AND (d.is_deleted = 0 OR d.is_deleted IS NULL)
SET mr.doctor_id = d.id
WHERE mr.doctor_id IS NULL AND mr.doctor_name IS NOT NULL AND mr.doctor_name != '';