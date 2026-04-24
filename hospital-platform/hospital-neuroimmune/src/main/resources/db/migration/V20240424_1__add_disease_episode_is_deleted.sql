-- 为 disease_episode 表添加逻辑删除字段
ALTER TABLE disease_episode ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '0-有效, 1-无效';