package org.hospital.common.audit;

/**
 * 操作类型枚举
 */
public enum OperationType {

    // 登录相关
    LOGIN("登录"),
    LOGOUT("登出"),
    KICK_OFFLINE("踢下线"),

    // 数据操作
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    QUERY("查询"),
    EXPORT("导出"),
    IMPORT("导入"),

    // 权限相关
    CHANGE_PASSWORD("修改密码"),
    GRANT_PERMISSION("授权"),
    REVOKE_PERMISSION("撤销权限"),

    // 文件操作
    UPLOAD("上传"),
    DOWNLOAD("下载"),

    // 系统操作
    SYSTEM_CONFIG("系统配置"),
    OTHER("其他");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}