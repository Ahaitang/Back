package org.hospital.common.model;

import org.hospital.common.enums.RecordStatus;

/**
 * 状态可管理接口
 * 提供记录状态的统一操作方法
 */
public interface StatusManageable {
    /**
     * 判断是否进行中
     */
    boolean isOngoing();

    /**
     * 判断是否已完成
     */
    boolean isCompleted();

    /**
     * 判断是否已取消
     */
    boolean isCancelled();

    /**
     * 标记为进行中
     */
    void markOngoing();

    /**
     * 标记为已完成
     */
    void markCompleted();

    /**
     * 标记为已取消
     */
    void markCancelled();

    /**
     * 获取状态枚举
     */
    RecordStatus getRecordStatus();
}