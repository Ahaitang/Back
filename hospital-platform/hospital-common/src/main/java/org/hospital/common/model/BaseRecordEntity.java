package org.hospital.common.model;

import org.hospital.common.enums.RecordStatus;

/**
 * 记录实体基类
 * 提供状态管理的通用实现
 */
public abstract class BaseRecordEntity implements StatusManageable {

    /**
     * 状态字段：0-进行中, 1-完成, 2-取消
     */
    protected Integer status;

    @Override
    public boolean isOngoing() {
        return RecordStatus.ONGOING.getCode() == (status != null ? status : 0);
    }

    @Override
    public boolean isCompleted() {
        return RecordStatus.COMPLETED.getCode() == (status != null ? status : 0);
    }

    @Override
    public boolean isCancelled() {
        return RecordStatus.CANCELLED.getCode() == (status != null ? status : 0);
    }

    @Override
    public void markOngoing() {
        this.status = RecordStatus.ONGOING.getCode();
    }

    @Override
    public void markCompleted() {
        this.status = RecordStatus.COMPLETED.getCode();
    }

    @Override
    public void markCancelled() {
        this.status = RecordStatus.CANCELLED.getCode();
    }

    @Override
    public RecordStatus getRecordStatus() {
        return RecordStatus.fromCode(status != null ? status : 0);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}