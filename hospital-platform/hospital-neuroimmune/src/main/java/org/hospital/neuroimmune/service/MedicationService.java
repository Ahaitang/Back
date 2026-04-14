package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;

public interface MedicationService {
    PageResult<Medication> getList(PageRequest request);
    PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request);
    Medication getById(Long id);
    void save(Medication medication);
    void delete(Long id);
    Long countByDoctorId(Long doctorId);
}