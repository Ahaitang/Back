package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import java.util.List;
import java.util.Map;

public interface FollowUpService {
    PageResult<FollowUp> getList(PageRequest request);
    PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request);
    FollowUp getById(Long id);
    void save(FollowUp followUp);
    void updateStatus(Long id, Integer status);
    void cancel(Long id);
    Long getPendingCount();
    Long getPendingCountByDoctorId(Long doctorId);
    Long getPendingCountByPatientId(Long patientId);
    Long getCompletedCount();
    Long getCompletedCountByDoctorId(Long doctorId);
    List<FollowUp> getPendingByDoctorId(Long doctorId);
    Long countByPatientId(Long patientId);

    /**
     * 取消患者的所有随访记录（患者删除时调用）
     */
    void cancelByPatientId(Long patientId);

    /**
     * 批量获取患者的待随访状态
     * @param patientIds 患者ID列表
     * @return Map<患者ID, 是否有待随访记录>
     */
    Map<Long, Boolean> batchGetPendingStatus(List<Long> patientIds);
}