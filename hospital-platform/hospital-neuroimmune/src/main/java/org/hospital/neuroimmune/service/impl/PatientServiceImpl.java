package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("neuroimmunePatientService")
public class PatientServiceImpl implements PatientService {

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private PatientDoctorRelationService relationService;

    @Override
    public PageResult<Patient> getList(PageRequest request) {
        Page<Patient> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Patient> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(Patient::getUpdateTime);

        Page<Patient> result = patientMapper.selectPage(page, wrapper);
        List<Patient> patients = result.getRecords();

        // Populate doctorName from relation table
        populateDoctorNames(patients);

        return new PageResult<>(patients, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Patient> getListByDoctorId(Long doctorId, PageRequest request) {
        // Get active patient relations for this doctor
        List<PatientDoctorRelation> relations = relationService.getActivePatientsByDoctor(doctorId);
        if (relations.isEmpty()) {
            return new PageResult<>(List.of(), 0L, request.getPageNum(), request.getPageSize());
        }

        List<Long> patientIds = relations.stream()
                .map(PatientDoctorRelation::getPatientId)
                .collect(Collectors.toList());

        // Query patients by IDs
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Patient::getId, patientIds);
        wrapper.eq(Patient::getIsDeleted, 0).or().isNull(Patient::getIsDeleted);
        wrapper.orderByDesc(Patient::getUpdateTime);

        Page<Patient> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Patient> result = patientMapper.selectPage(page, wrapper);
        List<Patient> patients = result.getRecords();

        // Set doctorName from relation
        patients.forEach(p -> {
            PatientDoctorRelation relation = relations.stream()
                    .filter(r -> r.getPatientId().equals(p.getId()))
                    .findFirst().orElse(null);
            if (relation != null) {
                p.setDoctorName(relation.getDoctorName());
            }
        });

        return new PageResult<>(patients, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    /**
     * Populate doctorName for patients from relation table
     */
    private void populateDoctorNames(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) return;

        List<Long> patientIds = patients.stream()
                .map(Patient::getId)
                .collect(Collectors.toList());

        Map<Long, String> doctorNameMap = relationService.batchGetDoctorNames(patientIds);

        patients.forEach(p -> {
            p.setDoctorName(doctorNameMap.get(p.getId()));
        });
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Patient> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();

        // 只查询有效数据
        wrapper.eq(Patient::getIsDeleted, 0).or().isNull(Patient::getIsDeleted);

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
        // doctorId filter is handled separately in getListByDoctorId
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(Patient::getDiseaseType, request.getType());
        }

        return wrapper;
    }

    @Override
    @Cacheable(value = "neuro-patient", key = "#id", unless = "#result == null")
    public Patient getById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            PatientDoctorRelation relation = relationService.getActiveDoctor(id);
            if (relation != null) {
                patient.setDoctorName(relation.getDoctorName());
            }
        }
        return patient;
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
        // 清除时间字段，让数据库自动处理
        patient.setCreateTime(null);
        patient.setUpdateTime(null);

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
    public Long countByDoctorId(Long doctorId) {
        return relationService.countPatientsByDoctor(doctorId);
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
        Patient patient = getById(id);
        if (patient != null) {
            return new PageResult<>(java.util.Collections.singletonList(patient), 1L, 1, 10);
        }
        return new PageResult<>(java.util.Collections.emptyList(), 0L, 1, 10);
    }
}