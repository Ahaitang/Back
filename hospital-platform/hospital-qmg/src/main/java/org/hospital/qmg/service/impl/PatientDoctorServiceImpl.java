package org.hospital.qmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.PatientDoctor;
import org.hospital.qmg.mapper.PatientDoctorMapper;
import org.hospital.qmg.service.PatientDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者-医生对应Service实现类
 */
@Slf4j
@Service
public class PatientDoctorServiceImpl implements PatientDoctorService {

    @Autowired
    private PatientDoctorMapper patientDoctorMapper;

    @Override
    @Transactional
    public void addMapping(Integer patientId, Integer doctorId) {
        log.info("添加患者-医生对应关系: patientId={}, doctorId={}", patientId, doctorId);
        
        // 检查是否已存在
        PatientDoctor existing = patientDoctorMapper.findByPatientIdAndDoctorId(patientId, doctorId);
        if (existing != null) {
            log.warn("患者-医生对应关系已存在: patientId={}, doctorId={}", patientId, doctorId);
            return;
        }
        
        PatientDoctor patientDoctor = new PatientDoctor();
        patientDoctor.setPatientId(patientId);
        patientDoctor.setDoctorId(doctorId);
        patientDoctor.setCreateTime(LocalDateTime.now());
        patientDoctor.setUpdateTime(LocalDateTime.now());
        
        patientDoctorMapper.insert(patientDoctor);
        log.info("添加患者-医生对应关系成功: patientId={}, doctorId={}", patientId, doctorId);
    }

    @Override
    @Transactional
    public void removeMapping(Integer patientId, Integer doctorId) {
        log.info("删除患者-医生对应关系: patientId={}, doctorId={}", patientId, doctorId);
        patientDoctorMapper.deleteByPatientIdAndDoctorId(patientId, doctorId);
    }

    @Override
    public List<Integer> findDoctorIdsByPatientId(Integer patientId) {
        return patientDoctorMapper.findDoctorIdsByPatientId(patientId);
    }

    @Override
    public Integer getCreatorDoctorIdByPatientId(Integer patientId) {
        if (patientId == null) return null;
        return patientDoctorMapper.findCreatorDoctorIdByPatientId(patientId);
    }

    @Override
    public List<Integer> findPatientIdsByDoctorId(Integer doctorId) {
        return patientDoctorMapper.findPatientIdsByDoctorId(doctorId);
    }

    @Override
    public boolean existsMapping(Integer patientId, Integer doctorId) {
        PatientDoctor mapping = patientDoctorMapper.findByPatientIdAndDoctorId(patientId, doctorId);
        return mapping != null;
    }

    @Override
    @Transactional
    public void deleteByPatientId(Integer patientId) {
        log.info("删除患者的所有对应关系: patientId={}", patientId);
        patientDoctorMapper.deleteByPatientId(patientId);
    }

    @Override
    @Transactional
    public void deleteByDoctorId(Integer doctorId) {
        log.info("删除医生的所有对应关系: doctorId={}", doctorId);
        patientDoctorMapper.deleteByDoctorId(doctorId);
    }
}
