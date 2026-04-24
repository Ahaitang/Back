-- 迁移 patient.disease_type 到 patient_disease 关联表
-- 解析逗号分隔的 disease_type 值并插入到 patient_disease

-- 先处理单值条目
INSERT IGNORE INTO patient_disease (patient_id, disease_code, create_time)
SELECT id, TRIM(disease_type), create_time
FROM patient
WHERE disease_type IS NOT NULL
  AND disease_type != ''
  AND disease_type NOT LIKE '%,%'
  AND (is_deleted = 0 OR is_deleted IS NULL);

-- 处理双值条目 (如 'MS,NMOSD')
INSERT IGNORE INTO patient_disease (patient_id, disease_code, create_time)
SELECT id, TRIM(SUBSTRING_INDEX(disease_type, ',', 1)), create_time
FROM patient
WHERE disease_type LIKE '%,%'
  AND disease_type NOT LIKE '%,%,%'
  AND (is_deleted = 0 OR is_deleted IS NULL);

INSERT IGNORE INTO patient_disease (patient_id, disease_code, create_time)
SELECT id, TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(disease_type, ',', 2), ',', -1)), create_time
FROM patient
WHERE disease_type LIKE '%,%'
  AND disease_type NOT LIKE '%,%,%'
  AND (is_deleted = 0 OR is_deleted IS NULL);

-- 处理三值条目 (如 'MS,NMOSD,MG')
INSERT IGNORE INTO patient_disease (patient_id, disease_code, create_time)
SELECT id, TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(disease_type, ',', 3), ',', -1)), create_time
FROM patient
WHERE disease_type LIKE '%,%,%'
  AND (is_deleted = 0 OR is_deleted IS NULL);