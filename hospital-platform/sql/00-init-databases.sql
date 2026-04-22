-- ============================================
-- 00. 初始化所有数据库
-- 执行顺序: 第一个执行
-- ============================================

-- 创建 QMG 数据库
CREATE DATABASE IF NOT EXISTS QMG
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 创建 neuroimmune 数据库
CREATE DATABASE IF NOT EXISTS neuroimmune
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 创建 session_audit 数据库
CREATE DATABASE IF NOT EXISTS session_audit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 提示
SELECT '数据库初始化完成！' AS status;