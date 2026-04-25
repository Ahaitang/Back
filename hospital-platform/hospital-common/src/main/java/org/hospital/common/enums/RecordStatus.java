package org.hospital.common.enums;

/**
 * 记录状态枚举
 * 用于随访、病历、用药等记录的状态统一管理
 */
public enum RecordStatus {
    ONGOING(0, "进行中"),
    COMPLETED(1, "已完成"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String description;

    RecordStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RecordStatus fromCode(int code) {
        for (RecordStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public boolean isOngoing() {
        return this == ONGOING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}