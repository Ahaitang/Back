-- V20240423_3__migrate_special_dicts.sql
-- 将专用字典数据迁移到通用字典表

-- 1. 迁移用药频率
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active)
SELECT 'frequency', code, name, description, sort_order, 1
FROM frequency_dict WHERE is_deleted = 0 OR is_deleted IS NULL;

-- 2. 迁移用药途径
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active)
SELECT 'route', code, name, description, sort_order, 1
FROM route_dict WHERE is_deleted = 0 OR is_deleted IS NULL;

-- 3. 迁移药品字典
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active)
SELECT 'medication', NULL, name,
       CONCAT_WS(' | ', generic_name, specification, manufacturer),
       id, 1
FROM medication_dict WHERE is_deleted = 0 OR is_deleted IS NULL;

-- 4. 迁移疾病类型
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active)
SELECT 'disease', code, name, description, sort_order, 1
FROM disease_dict WHERE is_deleted = 0 OR is_deleted IS NULL;

-- 5. 新增用药单位字典
INSERT INTO dict_common (dict_type, code, name, sort_order, is_active) VALUES
('medicationUnit', 'MG', 'mg', 1, 1),
('medicationUnit', 'G', 'g', 2, 1),
('medicationUnit', 'ML', 'ml', 3, 1),
('medicationUnit', 'TABLET', '片', 4, 1),
('medicationUnit', 'CAPSULE', '粒', 5, 1);

-- 6. 新增角色字典
INSERT INTO dict_common (dict_type, code, name, description, sort_order, is_active) VALUES
('role', 'DOCTOR', '普通医生', '仅医生权限', 1, 1),
('role', 'ADMIN', '管理员', '仅管理员权限', 2, 1),
('role', 'DOCTOR_ADMIN', '医生+管理员', '双重权限', 3, 1);