package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("neuroimmunePatientService")
public class PatientServiceImpl implements PatientService {

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Override
    public PageResult<Patient> getList(PageRequest request) {
        Page<Patient> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Patient> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(Patient::getUpdateTime);

        Page<Patient> result = patientMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Patient> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<Patient> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Patient> wrapper = buildQueryWrapper(request);
        wrapper.eq(Patient::getDoctorId, doctorId);
        wrapper.orderByDesc(Patient::getUpdateTime);

        Page<Patient> result = patientMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Patient> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Patient::getName, request.getKeyword())
                    .or().like(Patient::getPhone, request.getKeyword())
                    .or().like(Patient::getId, request.getKeyword()));
        }
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            wrapper.eq(Patient::getGender, request.getGender());
        }
        if (request.getIsRealAuth() != null) {
            wrapper.eq(Patient::getIsRealAuth, request.getIsRealAuth());
        }
        if (request.getDoctorId() != null) {
            wrapper.eq(Patient::getDoctorId, request.getDoctorId());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(Patient::getDiseaseType, request.getType());
        }

        return wrapper;
    }

    @Override
    @Cacheable(value = "neuro-patient", key = "#id", unless = "#result == null")
    public Patient getById(Long id) {
        return patientMapper.selectById(id);
    }

    @Override
    public Patient login(LoginRequest request) {
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
    public void batchInsert(List<Patient> patients) {
        for (Patient patient : patients) {
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