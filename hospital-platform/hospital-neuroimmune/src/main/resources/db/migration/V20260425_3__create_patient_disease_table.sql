-- 创建患者-疾病关联表，替代 patient 表的 disease_type 逗号分隔存储
CREATE TABLE patient_disease (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    disease_code VARCHAR(20) NOT NULL COMMENT '疾病编码: MS, NMOSD, MG, MOGAD, AUTO_ENCEPHALITIS, GBS, CIDP, OTHER',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_disease (patient_id, disease_code),
    INDEX idx_patient_id (patient_id),
    INDEX idx_disease_code (disease_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者疾病关联表';
