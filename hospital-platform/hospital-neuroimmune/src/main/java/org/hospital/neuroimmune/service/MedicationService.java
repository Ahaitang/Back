package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Medication;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import java.util.List;

public interface MedicationService {
    PageResult<Medication> getList(PageRequest request);
    PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request);
    Medication getById(Long id);
    List<Medication> getByPatientId(Long patientId);
    List<Medication> getByDoctorId(Long doctorId);
    List<Medication> getAllMedications();
    void save(Medication medication);
    void updateStatus(Long id, Integer status);
    void cancel(Long id);
    Long countByDoctorId(Long doctorId);
    Long countByPatientId(Long patientId);
    void deleteByPatientId(Long patientId);
}