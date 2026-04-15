package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.PatientDoctorRelationMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientDoctorRelationServiceImpl implements PatientDoctorRelationService {

    @Autowired
    private PatientDoctorRelationMapper relationMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

    @Override
    @Transactional
    public boolean bindDoctor(Long patientId, Long doctorId, String bindMethod, String remark) {
        // 检查患者和医生是否存在
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }

        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
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
        relation.setDoctorName(doctor.getName());
        relation.setRelationType("primary");
        relation.setStatus("active");
        relation.setBindMethod(bindMethod != null ? bindMethod : "patient");
        relation.setRemark(remark);
        relation.setBindTime(LocalDateTime.now());

        int result = relationMapper.insert(relation);

        // 更新患者表中的医生信息（兼容旧数据）
        patient.setDoctorId(doctorId);
        patient.setDoctorName(doctor.getName());
        patientMapper.updateById(patient);

        // 更新医生的患者数量
        updateDoctorPatientCount(doctorId);

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

        // 更新患者表中的医生信息
        Patient patient = patientMapper.selectById(relation.getPatientId());
        if (patient != null && relation.getDoctorId().equals(patient.getDoctorId())) {
            patient.setDoctorId(null);
            patient.setDoctorName(null);
            patientMapper.updateById(patient);
        }

        // 更新医生的患者数量
        updateDoctorPatientCount(relation.getDoctorId());

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

    /**
     * 更新医生的患者数量
     */
    private void updateDoctorPatientCount(Long doctorId) {
        Long count = relationMapper.countByDoctorId(doctorId);
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            doctor.setPatientCount(count != null ? count.intValue() : 0);
            doctorMapper.updateById(doctor);
        }
    }
}