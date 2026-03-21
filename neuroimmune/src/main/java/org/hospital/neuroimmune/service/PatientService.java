package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.dto.LoginRequest;

import java.util.List;

public interface PatientService {
    PageResult<Patient> getList(PageRequest request);
    PageResult<Patient> getListByDoctorId(Long doctorId, PageRequest request);
    Patient getById(Long id);
    Patient login(LoginRequest request);
    void save(Patient patient);
    void updatePassword(Long id, String password);
    void delete(Long id);
    Long countByDoctorId(Long doctorId);
    void batchInsert(List<Patient> patients);
    Patient getByPhone(String phone);

    /**
     * 根据ID获取单个患者信息，封装为PageResult返回（患者端用）
     */
    PageResult<Patient> getByIdAsPageResult(Long id);
}