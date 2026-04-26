-- 为病历记录表添加关联发作记录字段
-- 执行时间: 2026-04-25

ALTER TABLE medical_record
ADD COLUMN related_episode_id bigint NULL COMMENT '关联的发作记录ID' AFTER notes;

-- 添加索引以优化查询
CREATE INDEX idx_mr_related_episode
ON medical_record (related_episode_id);

-- 说明：
-- related_episode_id 用于关联疾病发作记录(disease_episode表)
-- 前端在发作记录详情页上传病历时会自动填充此字段
-- 病历列表显示时会返回关联的发作次数(relatedEpisodeNumber)