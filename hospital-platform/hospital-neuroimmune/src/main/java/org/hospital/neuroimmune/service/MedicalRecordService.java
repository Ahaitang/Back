package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import java.util.List;

public interface MedicalRecordService {
    PageResult<MedicalRecord> getList(PageRequest request);
    PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request);
    MedicalRecord getById(Long id);
    List<MedicalRecord> getByPatientId(Long patientId);
    void save(MedicalRecord record);
    void updateStatus(Long id, Integer status);
    void cancel(Long id);
    Long countByPatientId(Long patientId);
}