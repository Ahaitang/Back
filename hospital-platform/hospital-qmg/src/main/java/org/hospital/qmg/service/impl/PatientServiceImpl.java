package org.hospital.qmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.Doctor;
import org.hospital.qmg.entity.Patient;
import org.hospital.qmg.mapper.QmgPatientMapper;
import org.hospital.common.model.ImportResult;
import org.hospital.qmg.service.PatientService;
import org.hospital.qmg.service.PatientDoctorService;
import org.hospital.qmg.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 患者Service实现类
 */
@Slf4j
@Service("qmgPatientService")
public class PatientServiceImpl implements PatientService {

    @Autowired
    private QmgPatientMapper patientMapper;
    
    @Autowired
    private PatientDoctorService patientDoctorService;

    @Autowired
    private DoctorService doctorService;

    @Override
    public List<Patient> findAll(Integer currentDoctorId, Integer currentUserLevel) {
        log.info("查询患者列表，当前医生ID: {}, 权限等级: {}", currentDoctorId, currentUserLevel);
        
        if (currentUserLevel != null && currentUserLevel == 0) {
            return patientMapper.findAll();
        }
        if (currentUserLevel != null && currentUserLevel == 1) {
            return patientMapper.findAllVisibleToAdmin();
        }
        if (currentDoctorId != null) {
            return patientMapper.findByDoctorId(currentDoctorId);
        }
        return Collections.emptyList();
    }

    @Override
    public Patient findById(Integer id, Integer currentDoctorId, Integer currentUserLevel) {
        log.info("根据ID查询患者: {}, 当前医生ID: {}, 权限等级: {}", id, currentDoctorId, currentUserLevel);
        
        Patient patient = patientMapper.findById(id);
        if (patient == null) {
            return null;
        }
        
        if (currentUserLevel != null && currentUserLevel == 0) {
            return patient;
        }
        if (currentUserLevel != null && currentUserLevel == 1) {
            Integer creatorDoctorId = patientDoctorService.getCreatorDoctorIdByPatientId(id);
            if (creatorDoctorId != null) {
                Doctor creator = doctorService.findById(creatorDoctorId);
                if (creator != null && creator.getLevel() != null && creator.getLevel() == 0) {
                    return null;
                }
            }
            return patient;
        }
        if (currentDoctorId != null && patientDoctorService.existsMapping(id, currentDoctorId)) {
            return patient;
        }
        return null;
    }

    @Override
    @Cacheable(value = "qmg-patient", key = "'admission:' + #admissionNumber", unless = "#result == null")
    public Patient findByAdmissionNumber(String admissionNumber) {
        log.info("根据住院号查询患者: {}", admissionNumber);
        return patientMapper.findByAdmissionNumber(admissionNumber);
    }

    @Override
    public List<Patient> findByDoctorIdOnly(Integer doctorId) {
        if (doctorId == null) {
            return Collections.emptyList();
        }
        return patientMapper.findByDoctorId(doctorId);
    }

    @Override
    public List<Patient> searchByDoctorIdAndKeywordOnly(Integer doctorId, String keyword) {
        if (doctorId == null) {
            return Collections.emptyList();
        }
        return patientMapper.searchByDoctorIdAndKeyword(doctorId, keyword != null ? keyword : "");
    }

    @Override
    public void linkDoctor(Integer patientId, Integer doctorId) {
        if (patientId == null || doctorId == null) {
            return;
        }
        patientDoctorService.addMapping(patientId, doctorId);
    }

    @Override
    public List<Patient> searchByNameOrAdmissionNumber(String keyword, Integer currentDoctorId, Integer currentUserLevel) {
        log.info("搜索患者，关键词: {}, 当前医生ID: {}, 权限等级: {}", keyword, currentDoctorId, currentUserLevel);
        
        if (currentUserLevel != null && currentUserLevel == 0) {
            return patientMapper.searchByNameOrAdmissionNumber(keyword != null ? keyword : "");
        }
        if (currentUserLevel != null && currentUserLevel == 1) {
            return patientMapper.searchByNameOrAdmissionNumberVisibleToAdmin(keyword != null ? keyword : "");
        }
        if (currentDoctorId != null) {
            return patientMapper.searchByDoctorIdAndKeyword(currentDoctorId, keyword != null ? keyword : "");
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Patient patient) {
        log.info("新增患者: {}", patient);
        patient.setCreateTime(LocalDateTime.now());
        patient.setUpdateTime(LocalDateTime.now());
        patientMapper.insert(patient);
    }

    @Override
    @CacheEvict(value = "qmg-patient", key = "'id:' + #patient.id")
    public void update(Patient patient) {
        log.info("更新患者信息: {}", patient);
        patient.setUpdateTime(LocalDateTime.now());
        patientMapper.update(patient);
    }

    @Override
    @CacheEvict(value = "qmg-patient", allEntries = true)
    public void deleteById(Integer id) {
        log.info("删除患者: {}", id);
        // 删除患者时，会自动删除对应的患者-医生关系（外键级联删除）
        patientMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult batchImport(List<Patient> patients, Integer currentDoctorId) {
        ImportResult result = new ImportResult();
        result.setTotal(patients != null ? patients.size() : 0);
        if (patients == null || patients.isEmpty()) {
            return result;
        }
        for (int i = 0; i < patients.size(); i++) {
            int row = i + 1;
            Patient p = patients.get(i);
            if (p == null) {
                result.addError(row, "数据为空");
                continue;
            }
            if (p.getName() == null || p.getName().trim().isEmpty()) {
                result.addError(row, "姓名为空");
                continue;
            }
            if (p.getGender() == null || p.getGender().trim().isEmpty()) {
                result.addError(row, "性别为空");
                continue;
            }
            if (!"male".equals(p.getGender()) && !"female".equals(p.getGender())) {
                result.addError(row, "性别必须为 male 或 female");
                continue;
            }
            if (p.getAdmissionNumber() == null || p.getAdmissionNumber().trim().isEmpty()) {
                result.addError(row, "住院号为空");
                continue;
            }
            if (p.getPhone() == null || p.getPhone().trim().isEmpty()) {
                result.addError(row, "联系电话为空");
                continue;
            }
            Patient existing = findByAdmissionNumber(p.getAdmissionNumber());
            if (existing != null) {
                result.addError(row, "住院号已存在: " + p.getAdmissionNumber());
                continue;
            }
            try {
                save(p);
                if (currentDoctorId != null) {
                    patientDoctorService.addMapping(p.getId(), currentDoctorId);
                }
                result.success();
            } catch (Exception e) {
                log.warn("批量导入第{}行失败: {}", row, e.getMessage());
                result.addError(row, e.getMessage() != null ? e.getMessage() : "保存失败");
            }
        }
        return result;
    }
}
