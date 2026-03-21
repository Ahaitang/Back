package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import java.util.List;

public interface FollowUpService {
    PageResult<FollowUp> getList(PageRequest request);
    PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request);
    FollowUp getById(Long id);
    void save(FollowUp followUp);
    void updateStatus(Long id, String status);
    void delete(Long id);
    Long getPendingCount();
    Long getPendingCountByDoctorId(Long doctorId);
    Long getPendingCountByPatientId(Long patientId);
    List<FollowUp> getPendingByDoctorId(Long doctorId);
}