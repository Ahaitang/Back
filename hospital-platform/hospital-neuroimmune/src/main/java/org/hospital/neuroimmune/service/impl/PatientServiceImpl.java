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
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.neuroimmune.service.PatientDiseaseService;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.hospital.neuroimmune.service.MedicationService;
import org.hospital.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("neuroimmunePatientService")
public class PatientServiceImpl implements PatientService {

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private PatientDoctorRelationService relationService;

    @Autowired
    private DiseaseEpisodeService diseaseEpisodeService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private PatientDiseaseService patientDiseaseService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private MedicationService medicationService;

    @Override
    public PageResult<Patient> getList(PageRequest request) {
        Page<Patient> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Patient> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(Patient::getUpdateTime);

        Page<Patient> result = patientMapper.selectPage(page, wrapper);
        List<Patient> patients = result.getRecords();

        // Populate doctorName from relation table
        populateDoctorNames(patients);
        // Populate diseaseTypes from patient_disease table
        populateDiseaseTypes(patients);
        // Populate computed status fields
        populateRealAuthStatus(patients);
        populateFollowUpStatus(patients);

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
                p.setDoctorId(relation.getDoctorId());
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

        Map<Long, PatientDoctorRelation> doctorMap = relationService.batchGetActiveDoctorInfo(patientIds);

        patients.forEach(p -> {
            PatientDoctorRelation relation = doctorMap.get(p.getId());
            if (relation != null) {
                p.setDoctorId(relation.getDoctorId());
                p.setDoctorName(relation.getDoctorName());
            }
        });
    }

    /**
     * Populate diseaseTypes for patients from patient_disease table
     */
    private void populateDiseaseTypes(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) return;

        patients.forEach(p -> {
            List<String> diseaseTypes = patientDiseaseService.getDiseaseCodesByPatientId(p.getId());
            p.setDiseaseTypes(diseaseTypes);
        });
    }

    /**
     * 批量计算患者实名状态
     * 根据idCard是否非空设置isRealAuth
     */
    private void populateRealAuthStatus(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) return;
        patients.forEach(p -> {
            p.setIsRealAuth(p.getIdCard() != null && !p.getIdCard().isEmpty());
        });
    }

    /**
     * 批量计算患者待随访状态
     * 根据是否有ONGOING状态的随访记录设置hasFollowUp
     */
    private void populateFollowUpStatus(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) return;

        List<Long> patientIds = patients.stream()
                .map(Patient::getId)
                .collect(Collectors.toList());

        // 批量查询每个患者是否有进行中的随访记录
        Map<Long, Boolean> followUpStatusMap = followUpService.batchGetPendingStatus(patientIds);

        patients.forEach(p -> {
            p.setHasFollowUp(followUpStatusMap.getOrDefault(p.getId(), false));
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
        // Disease type filter - now uses patient_disease table
        if (request.getType() != null && !request.getType().isEmpty()) {
            List<Long> patientIdsWithType = patientDiseaseService.getPatientIdsByDiseaseCode(request.getType());
            if (!patientIdsWithType.isEmpty()) {
                wrapper.in(Patient::getId, patientIdsWithType);
            } else {
                // No patients with this disease type, return empty result
                wrapper.apply("1 = 0");
            }
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
                patient.setDoctorId(relation.getDoctorId());
                patient.setDoctorName(relation.getDoctorName());
            }
            // 查询患者的疾病类型列表
            List<String> diseaseTypes = patientDiseaseService.getDiseaseCodesByPatientId(id);
            patient.setDiseaseTypes(diseaseTypes);
            // 计算实名状态
            patient.setIsRealAuth(patient.getIdCard() != null && !patient.getIdCard().isEmpty());
            // 计算待随访状态
            patient.setHasFollowUp(followUpService.getPendingCountByPatientId(id) > 0);
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
    @Transactional
    public void save(Patient patient) {
        // 清除时间字段，让数据库自动处理
        patient.setCreateTime(null);
        patient.setUpdateTime(null);

        // 校验手机号
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            Patient existing = patientMapper.selectByPhone(patient.getPhone());
            if (existing != null) {
                // 新增时：手机号已存在则报错
                // 编辑时：如果手机号被其他用户占用则报错
                if (patient.getId() == null || !patient.getId().equals(existing.getId())) {
                    throw new RuntimeException("手机号 " + patient.getPhone() + " 已被其他患者使用");
                }
            }
        }

        if (patient.getId() == null) {
            if (patient.getPassword() != null && !patient.getPassword().startsWith("$2")) {
                patient.setPassword(PasswordUtil.encode(patient.getPassword()));
            }
            patientMapper.insert(patient);
        } else {
            patientMapper.updateById(patient);
        }

        // 同步疾病类型到 patient_disease 表
        if (patient.getDiseaseTypes() != null && patient.getId() != null) {
            patientDiseaseService.setDiseasesForPatient(patient.getId(), patient.getDiseaseTypes());
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
    @Transactional
    public void delete(Long id) {
        // 逻辑删除患者
        LambdaUpdateWrapper<Patient> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Patient::getId, id).set(Patient::getIsDeleted, 1);
        patientMapper.update(null, updateWrapper);

        // 同步设置关联信息为无效
        // 1. 解绑患者-医生关系
        relationService.unbindAllByPatientId(id);

        // 2. 逻辑删除患者的发作记录
        diseaseEpisodeService.deleteByPatientId(id);

        // 3. 取消患者的随访记录
        followUpService.cancelByPatientId(id);

        // 4. 逻辑删除患者的病历记录
        medicalRecordService.deleteByPatientId(id);

        // 5. 逻辑删除患者的用药记录
        medicationService.deleteByPatientId(id);
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