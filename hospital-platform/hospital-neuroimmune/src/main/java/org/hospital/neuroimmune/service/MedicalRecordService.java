package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;

public interface MedicalRecordService {
    PageResult<MedicalRecord> getList(PageRequest request);
    PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request);
    MedicalRecord getById(Long id);
    void save(MedicalRecord record);
    void delete(Long id);
}