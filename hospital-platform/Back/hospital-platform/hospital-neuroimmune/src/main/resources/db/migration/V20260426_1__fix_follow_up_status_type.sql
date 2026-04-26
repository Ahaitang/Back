-- 修复 follow_up 表的 status 字段类型
-- 将 VARCHAR 状态转换为 INT 状态（0-进行中, 1-已完成, 2-已取消）

-- 1. 先将现有数据转换为整数
UPDATE follow_up SET status = '0' WHERE status = 'pending' OR status = '进行中';
UPDATE follow_up SET status = '1' WHERE status = 'completed' OR status = '已完成';
UPDATE follow_up SET status = '2' WHERE status = 'cancelled' OR status = '已取消';

-- 2. 删除 status_text 字段（不再需要）
ALTER TABLE follow_up DROP COLUMN IF EXISTS status_text;

-- 3. 修改 status 字段类型为 INT
ALTER TABLE follow_up MODIFY COLUMN status INT DEFAULT 0 COMMENT '状态：0-进行中, 1-已完成, 2-已取消';

-- 4. 添加 status 索引（如果不存在）
ALTER TABLE follow_up ADD INDEX IF NOT EXISTS idx_fu_status_int (status);