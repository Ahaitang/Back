package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.dto.LoginRequest;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.mapper.PatientMapper;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public PageResult<Patient> getList(PageRequest request) {
        List<Patient> list = patientMapper.selectList(request);
        Long total = patientMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    @Cacheable(value = "neuro-patient", key = "#id", unless = "#result == null")
    public Patient getById(Long id) {
        return patientMapper.selectById(id);
    }

    @Override
    public Patient login(LoginRequest request) {
        // 用户名可以是手机号
        Patient patient = patientMapper.selectByPhone(request.getUsername());
        if (patient != null && patient.getPassword() != null) {
            if (PasswordUtil.matches(request.getPassword(), patient.getPassword())) {
                return patient;
            }
        }
        return null;
    }

    @Override
    @CacheEvict(value = "neuro-patient", key = "#patient.id", condition = "#patient.id != null")
    public void save(Patient patient) {
        if (patient.getId() == null) {
            // 新增时加密密码
            if (patient.getPassword() != null && !patient.getPassword().startsWith("$2")) {
                patient.setPassword(PasswordUtil.encode(patient.getPassword()));
            }
            patientMapper.insert(patient);
        } else {
            patientMapper.updateById(patient);
        }
    }

    @Override
    public void updatePassword(Long id, String password) {
        LambdaUpdateWrapper<Patient> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Patient::getId, id)
                .set(Patient::getPassword, PasswordUtil.encode(password));
        patientMapper.update(null, updateWrapper);
    }

    @Override
    @CacheEvict(value = "neuro-patient", key = "#id")
    public void delete(Long id) {
        patientMapper.deleteById(id);
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'patient:count:doctor:' + #doctorId")
    public Long countByDoctorId(Long doctorId) {
        return patientMapper.selectCountByDoctorId(doctorId);
    }

    @Override
    public PageResult<Patient> getListByDoctorId(Long doctorId, PageRequest request) {
        request.setDoctorId(doctorId);
        List<Patient> list = patientMapper.selectList(request);
        Long total = patientMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public void batchInsert(List<Patient> patients) {
        for (Patient patient : patients) {
            // 加密密码
            if (patient.getPassword() != null && !patient.getPassword().startsWith("$2")) {
                patient.setPassword(PasswordUtil.encode(patient.getPassword()));
            }
            patientMapper.insert(patient);
        }
    }

    @Override
    public Patient getByPhone(String phone) {
        return patientMapper.selectByPhone(phone);
    }

    @Override
    public PageResult<Patient> getByIdAsPageResult(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            return new PageResult<>(java.util.Collections.singletonList(patient), 1L, 1, 10);
        }
        return new PageResult<>(java.util.Collections.emptyList(), 0L, 1, 10);
    }
}