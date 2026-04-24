package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.mapper.PatientDoctorRelationMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientDoctorRelationServiceImpl implements PatientDoctorRelationService {

    @Autowired
    private PatientDoctorRelationMapper relationMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Override
    @Transactional
    public boolean bindDoctor(Long patientId, Long doctorId, String bindMethod, String remark) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }

        // 检查是否已有生效中的绑定
        PatientDoctorRelation existing = relationMapper.selectActiveByPatientId(patientId);
        if (existing != null) {
            // 先解除旧绑定
            relationMapper.unbind(existing.getId());
        }

        // 检查是否已存在相同的绑定记录
        PatientDoctorRelation sameRelation = relationMapper.selectByPatientAndDoctor(patientId, doctorId);
        if (sameRelation != null) {
            // 重新激活
            sameRelation.setStatus("active");
            sameRelation.setBindTime(LocalDateTime.now());
            sameRelation.setUnbindTime(null);
            sameRelation.setRemark(remark);
            relationMapper.updateById(sameRelation);
            return true;
        }

        // 创建新绑定
        PatientDoctorRelation relation = new PatientDoctorRelation();
        relation.setPatientId(patientId);
        relation.setPatientName(patient.getName());
        relation.setDoctorId(doctorId);
        relation.setDoctorName(null);  // doctorName 可以后续查询填充
        relation.setRelationType("primary");
        relation.setStatus("active");
        relation.setBindMethod(bindMethod != null ? bindMethod : "patient");
        relation.setRemark(remark);
        relation.setBindTime(LocalDateTime.now());

        int result = relationMapper.insert(relation);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean unbind(Long id) {
        PatientDoctorRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            return false;
        }

        int result = relationMapper.unbind(id);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean unbindPatient(Long patientId) {
        PatientDoctorRelation relation = relationMapper.selectActiveByPatientId(patientId);
        if (relation == null) {
            return false;
        }
        return unbind(relation.getId());
    }

    @Override
    public PatientDoctorRelation getActiveDoctor(Long patientId) {
        return relationMapper.selectActiveByPatientId(patientId);
    }

    @Override
    public Map<Long, String> batchGetDoctorNames(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return Map.of();
        }
        List<PatientDoctorRelation> relations = relationMapper.selectBatchActiveByPatientIds(patientIds);
        return relations.stream()
                .filter(r -> r.getDoctorName() != null)
                .collect(Collectors.toMap(
                        PatientDoctorRelation::getPatientId,
                        PatientDoctorRelation::getDoctorName
                ));
    }

    @Override
    public Map<Long, PatientDoctorRelation> batchGetActiveDoctorInfo(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return Map.of();
        }
        List<PatientDoctorRelation> relations = relationMapper.selectBatchActiveByPatientIds(patientIds);
        return relations.stream()
                .collect(Collectors.toMap(
                        PatientDoctorRelation::getPatientId,
                        r -> r,
                        (a, b) -> a
                ));
    }

    @Override
    public List<PatientDoctorRelation> getPatientsByDoctor(Long doctorId) {
        return relationMapper.selectByDoctorId(doctorId);
    }

    @Override
    public List<PatientDoctorRelation> getActivePatientsByDoctor(Long doctorId) {
        return relationMapper.selectActiveByDoctorId(doctorId);
    }

    @Override
    public List<PatientDoctorRelation> getBindHistory(Long patientId) {
        return relationMapper.selectByPatientId(patientId);
    }

    @Override
    public List<PatientDoctorRelation> getList(String patientName, String doctorName, String status) {
        return relationMapper.selectRelationList(patientName, doctorName, status);
    }

    @Override
    public Long countPatientsByDoctor(Long doctorId) {
        return relationMapper.countByDoctorId(doctorId);
    }

    @Override
    public PatientDoctorRelation getById(Long id) {
        return relationMapper.selectById(id);
    }

    @Override
    public PatientDoctorRelation getByPatientAndDoctor(Long patientId, Long doctorId) {
        return relationMapper.selectByPatientAndDoctor(patientId, doctorId);
    }

    @Override
    @Transactional
    public void unbindAllByPatientId(Long patientId) {
        // 解除患者所有绑定关系
        relationMapper.unbindAllByPatientId(patientId);
    }

    @Override
    @Transactional
    public void unbindAllByDoctorId(Long doctorId) {
        // 解除医生所有绑定关系
        relationMapper.unbindAllByDoctorId(doctorId);
    }
}