package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import static org.hospital.neuroimmune.entity.PatientDoctorRelation.STATUS_ACTIVE;
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
            sameRelation.setStatus(STATUS_ACTIVE);
            sameRelation.setBindTime(LocalDateTime.now());
            sameRelation.setUnbindTime(null);
            sameRelation.setRemark(remark);
            relationMapper.updateById(sameRelation);
            return true;
        }

        // 创建新绑定
        PatientDoctorRelation relation = new PatientDoctorRelation();
        relation.setPatientId(patientId);
        relation.setDoctorId(doctorId);
        relation.setRelationType("primary");
        relation.setStatus(STATUS_ACTIVE);
        relation.setBindMethod(bindMethod != null ? bindMethod : "patient");
        relation.setRemark(remark);
        relation.setBindTime(LocalDateTime.now());
        // patientName/doctorName 不再存储，由 JOIN 查询时填充

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

    @Override
    public Map<Long, Long> countByDoctorIds(List<Long> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> results = relationMapper.countByDoctorIds(doctorIds);
        Map<Long, Long> countMap = new java.util.HashMap<>();
        for (Map<String, Object> row : results) {
            Long doctorId = ((Number) row.get("doctor_id")).longValue();
            Long count = ((Number) row.get("cnt")).longValue();
            countMap.put(doctorId, count);
        }
        return countMap;
    }

    @Override
    @Transactional
    public void createPendingRelation(PatientDoctorRelation relation) {
        relationMapper.insert(relation);
    }

    @Override
    public List<PatientDoctorRelation> getPendingRelationsByDoctor(Long doctorId) {
        return relationMapper.selectByDoctorIdAndBindStatus(doctorId, PatientDoctorRelation.BIND_STATUS_PENDING);
    }

    @Override
    public List<PatientDoctorRelation> getConfirmedRelationsByDoctor(Long doctorId) {
        return relationMapper.selectByDoctorIdAndBindStatus(doctorId, PatientDoctorRelation.BIND_STATUS_CONFIRMED);
    }

    @Override
    public List<PatientDoctorRelation> getRejectedRelationsByDoctor(Long doctorId) {
        return relationMapper.selectByDoctorIdAndBindStatus(doctorId, PatientDoctorRelation.BIND_STATUS_REJECTED);
    }

    @Override
    @Transactional
    public boolean confirmRelation(Long relationId) {
        PatientDoctorRelation relation = relationMapper.selectById(relationId);
        Integer currentStatus = relation != null ? relation.getBindStatus() : null;
        if (relation == null || currentStatus == null || !currentStatus.equals(PatientDoctorRelation.BIND_STATUS_PENDING)) {
            return false;
        }

        // 更新绑定状态
        relation.setBindStatus(PatientDoctorRelation.BIND_STATUS_CONFIRMED);
        relation.setConfirmTime(LocalDateTime.now());
        relationMapper.updateById(relation);

        // 更新患者状态为active
        patientMapper.updateStatus(relation.getPatientId(), Patient.STATUS_ACTIVE);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectRelation(Long relationId) {
        PatientDoctorRelation relation = relationMapper.selectById(relationId);
        Integer currentStatus = relation != null ? relation.getBindStatus() : null;
        if (relation == null || currentStatus == null || !currentStatus.equals(PatientDoctorRelation.BIND_STATUS_PENDING)) {
            return false;
        }

        // 更新绑定状态
        relation.setBindStatus(PatientDoctorRelation.BIND_STATUS_REJECTED);
        relation.setConfirmTime(LocalDateTime.now());
        relationMapper.updateById(relation);

        // 更新患者状态为rejected
        patientMapper.updateStatus(relation.getPatientId(), Patient.STATUS_REJECTED);

        return true;
    }
}