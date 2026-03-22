-- 为 medication 表添加 end_date 字段
-- 用于存储用药建议的结束日期

ALTER TABLE medication ADD COLUMN end_date DATETIME NULL COMMENT '结束日期';

-- 为已有记录更新 end_date（根据 duration 计算）
-- MySQL 兼容版本：使用 SUBSTRING 和 LOCATE 解析数字

-- 更新天
UPDATE medication
SET end_date = DATE_ADD(date, INTERVAL CAST(SUBSTRING(duration, 1, LOCATE('天', duration) - 1) AS UNSIGNED) DAY)
WHERE duration LIKE '%天%' AND end_date IS NULL;

-- 更新周
UPDATE medication
SET end_date = DATE_ADD(date, INTERVAL CAST(SUBSTRING(duration, 1, LOCATE('周', duration) - 1) AS UNSIGNED) WEEK)
WHERE duration LIKE '%周%' AND end_date IS NULL;

-- 更新月
UPDATE medication
SET end_date = DATE_ADD(date, INTERVAL CAST(SUBSTRING(duration, 1, LOCATE('月', duration) - 1) AS UNSIGNED) MONTH)
WHERE duration LIKE '%月%' AND end_date IS NULL;

-- 更新年
UPDATE medication
SET end_date = DATE_ADD(date, INTERVAL CAST(SUBSTRING(duration, 1, LOCATE('年', duration) - 1) AS UNSIGNED) YEAR)
WHERE duration LIKE '%年%' AND end_date IS NULL;

-- 其他情况默认一个月
UPDATE medication
SET end_date = DATE_ADD(date, INTERVAL 1 MONTH)
WHERE duration IS NOT NULL AND end_date IS NULL;

UPDATE medication SET
                      date = '2026-03-20',
                      end_date = '2026-04-20'
WHERE id = 1;

UPDATE medication SET
                      date = '2026-03-15',
                      end_date = '2026-04-15'
WHERE id = 2;