package org.hospital.neuroimmune.util;

import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.entity.CommonDict;
import org.hospital.neuroimmune.service.EntityQueryService;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.neuroimmune.mapper.CommonDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实体名称填充工具
 * 用于统一处理各实体的 patientName/doctorName 填充，避免重复代码
 */
@Component
public class EntityNameEnricher {

    @Autowired
    private EntityQueryService entityQueryService;

    @Autowired
    private DiseaseEpisodeService diseaseEpisodeService;

    @Autowired
    private CommonDictMapper commonDictMapper;

    /**
     * 填充随访记录的名称
     */
    public void enrichFollowUps(List<FollowUp> followUps) {
        if (followUps == null || followUps.isEmpty()) return;

        List<Long> patientIds = followUps.stream()
                .map(FollowUp::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> doctorIds = followUps.stream()
                .map(FollowUp::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = entityQueryService.batchGetPatients(patientIds);
        Map<Long, Doctor> doctorMap = entityQueryService.batchGetDoctors(doctorIds);

        followUps.forEach(fu -> {
            Patient p = patientMap.get(fu.getPatientId());
            if (p != null) {
                fu.setPatientName(p.getName());
                fu.setPatientGender(p.getGender());
                fu.setPatientAge(p.getAge());
            }
            if (fu.getDoctorId() != null) {
                Doctor d = doctorMap.get(fu.getDoctorId());
                if (d != null) fu.setDoctorName(d.getName());
            }
        });

        // 填充随访检查类型名称
        enrichFollowUpExamTypeName(followUps);
    }

    /**
     * 填充病历记录的名称
     */
    public void enrichMedicalRecords(List<MedicalRecord> records) {
        if (records == null || records.isEmpty()) return;

        List<Long> patientIds = records.stream()
                .map(MedicalRecord::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> doctorIds = records.stream()
                .map(MedicalRecord::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = entityQueryService.batchGetPatients(patientIds);
        Map<Long, Doctor> doctorMap = entityQueryService.batchGetDoctors(doctorIds);

        records.forEach(r -> {
            Patient p = patientMap.get(r.getPatientId());
            if (p != null) r.setPatientName(p.getName());
            if (r.getDoctorId() != null) {
                Doctor d = doctorMap.get(r.getDoctorId());
                if (d != null) r.setDoctorName(d.getName());
            }
            // 填充关联发作次数
            if (r.getRelatedEpisodeId() != null) {
                DiseaseEpisode episode = diseaseEpisodeService.getById(r.getRelatedEpisodeId());
                if (episode != null) {
                    r.setRelatedEpisodeNumber(episode.getEpisodeNumber());
                }
            }
        });
    }

    /**
     * 填充用药记录的名称
     */
    public void enrichMedications(List<Medication> medications) {
        if (medications == null || medications.isEmpty()) return;

        List<Long> patientIds = medications.stream()
                .map(Medication::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> doctorIds = medications.stream()
                .map(Medication::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = entityQueryService.batchGetPatients(patientIds);
        Map<Long, Doctor> doctorMap = entityQueryService.batchGetDoctors(doctorIds);

        medications.forEach(m -> {
            Patient p = patientMap.get(m.getPatientId());
            if (p != null) m.setPatientName(p.getName());
            if (m.getDoctorId() != null) {
                Doctor d = doctorMap.get(m.getDoctorId());
                if (d != null) m.setDoctorName(d.getName());
            }
        });
    }

    /**
     * 填充单条随访记录的名称
     */
    public void enrichSingleFollowUp(FollowUp followUp) {
        if (followUp == null) return;
        if (followUp.getPatientId() != null) {
            Patient p = entityQueryService.getPatient(followUp.getPatientId());
            if (p != null) {
                followUp.setPatientName(p.getName());
                followUp.setPatientGender(p.getGender());
                followUp.setPatientAge(p.getAge());
            }
        }
        if (followUp.getDoctorId() != null) {
            Doctor d = entityQueryService.getDoctor(followUp.getDoctorId());
            if (d != null) followUp.setDoctorName(d.getName());
        }
    }

    /**
     * 填充单条病历记录的名称
     */
    public void enrichSingleMedicalRecord(MedicalRecord record) {
        if (record == null) return;
        if (record.getPatientId() != null) {
            Patient p = entityQueryService.getPatient(record.getPatientId());
            if (p != null) record.setPatientName(p.getName());
        }
        if (record.getDoctorId() != null) {
            Doctor d = entityQueryService.getDoctor(record.getDoctorId());
            if (d != null) record.setDoctorName(d.getName());
        }
        // 填充关联发作次数
        if (record.getRelatedEpisodeId() != null) {
            DiseaseEpisode episode = diseaseEpisodeService.getById(record.getRelatedEpisodeId());
            if (episode != null) {
                record.setRelatedEpisodeNumber(episode.getEpisodeNumber());
            }
        }
    }

    /**
     * 填充随访检查类型名称
     */
    private void enrichFollowUpExamTypeName(List<FollowUp> followUps) {
        if (followUps == null || followUps.isEmpty()) return;

        // 收集所有 followUpExamTypeId
        Set<Long> typeIds = followUps.stream()
                .map(FollowUp::getFollowUpExamTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (typeIds.isEmpty()) return;

        // 批量查询（MyBatis-Plus BaseMapper 自带 selectBatchIds 方法）
        List<Long> typeIdList = new ArrayList<>(typeIds);
        List<CommonDict> dicts = commonDictMapper.selectBatchIds(typeIdList);

        Map<Long, String> typeNameMap = dicts.stream()
                .collect(Collectors.toMap(CommonDict::getId, CommonDict::getName));

        // 填充名称
        for (FollowUp fu : followUps) {
            if (fu.getFollowUpExamTypeId() != null) {
                fu.setFollowUpExamTypeName(typeNameMap.getOrDefault(fu.getFollowUpExamTypeId(), ""));
            }
        }
    }
}