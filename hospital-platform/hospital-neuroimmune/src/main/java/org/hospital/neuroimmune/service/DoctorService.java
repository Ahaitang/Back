package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.model.LoginRequest;

import java.util.List;

public interface DoctorService {
    PageResult<Doctor> getList(PageRequest request);
    Doctor getById(Long id);
    Doctor login(LoginRequest request);
    void save(Doctor doctor);
    void updatePassword(Long id, String password);
    void delete(Long id);
    void batchInsert(List<Doctor> doctors);
    Doctor getByPhone(String phone);
}