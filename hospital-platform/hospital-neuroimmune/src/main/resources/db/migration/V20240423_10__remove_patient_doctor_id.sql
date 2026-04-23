-- 简化医患关系设计 - 移除 patient.doctor_id
-- 医患关系统一使用 patient_doctor_relation 表管理

-- Step 1: 同步现有数据到 patient_doctor_relation
-- 对于已有 doctor_id 但没有对应 relation 记录的患者，创建绑定记录
INSERT INTO patient_doctor_relation (patient_id, patient_name, doctor_id, doctor_name, relation_type, status, bind_method, bind_time, create_time)
SELECT p.id, p.name, p.doctor_id, d.name, 'primary', 'active', 'system', NOW(), NOW()
FROM patient p
INNER JOIN doctor d ON p.doctor_id = d.id
WHERE p.doctor_id IS NOT NULL
  AND (p.is_deleted = 0 OR p.is_deleted IS NULL)
  AND (d.is_deleted = 0 OR d.is_deleted IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM patient_doctor_relation r
    WHERE r.patient_id = p.id AND r.doctor_id = p.doctor_id AND r.status = 'active'
  );

-- Step 2: 删除 patient 表的 doctor_id 列
ALTER TABLE patient DROP COLUMN doctor_id;