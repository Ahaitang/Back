-- 统一所有时间字段为 DATETIME（精确到时分秒）

-- disease_episode: episode_date
ALTER TABLE disease_episode MODIFY COLUMN episode_date DATETIME COMMENT '发作时间';

-- follow_up: date, hospitalization_time
ALTER TABLE follow_up MODIFY COLUMN date DATETIME COMMENT '随访日期';
ALTER TABLE follow_up MODIFY COLUMN hospitalization_time DATETIME COMMENT '住院时间';

-- medical_record: date
ALTER TABLE medical_record MODIFY COLUMN date DATETIME COMMENT '病历日期';

-- medication: date, end_date
ALTER TABLE medication MODIFY COLUMN date DATETIME COMMENT '用药开始日期';
ALTER TABLE medication MODIFY COLUMN end_date DATETIME COMMENT '用药结束日期';

-- patient: birth_date
ALTER TABLE patient MODIFY COLUMN birth_date DATETIME COMMENT '出生日期';