package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.EntityQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 实体查询服务实现
 * 作为跨模块查询的统一入口，避免其他 Service 直接操作 Mapper
 */
@Service
public class EntityQueryServiceImpl implements EntityQueryService {

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

    @Override
    public Map<Long, Patient> batchGetPatients(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> distinctIds = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return patientMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(Patient::getId, p -> p, (a, b) -> a));
    }

    @Override
    public Map<Long, Doctor> batchGetDoctors(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> distinctIds = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return doctorMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d, (a, b) -> a));
    }

    @Override
    public Patient getPatient(Long id) {
        if (id == null) return null;
        return patientMapper.selectById(id);
    }

    @Override
    public Doctor getDoctor(Long id) {
        if (id == null) return null;
        return doctorMapper.selectById(id);
    }

    @Override
    public Map<Long, String> batchGetPatientNames(List<Long> ids) {
        Map<Long, Patient> patients = batchGetPatients(ids);
        return patients.entrySet().stream()
                .filter(e -> e.getValue().getName() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));
    }

    @Override
    public Map<Long, String> batchGetDoctorNames(List<Long> ids) {
        Map<Long, Doctor> doctors = batchGetDoctors(ids);
        return doctors.entrySet().stream()
                .filter(e -> e.getValue().getName() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));
    }

    @Override
    public String getPatientName(Long id) {
        Patient patient = getPatient(id);
        return patient != null ? patient.getName() : null;
    }

    @Override
    public String getDoctorName(Long id) {
        Doctor doctor = getDoctor(id);
        return doctor != null ? doctor.getName() : null;
    }
}