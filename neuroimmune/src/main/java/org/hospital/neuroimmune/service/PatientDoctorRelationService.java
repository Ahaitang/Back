package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import java.util.List;

public interface PatientDoctorRelationService {

    /**
     * 绑定患者和医生
     */
    boolean bindDoctor(Long patientId, Long doctorId, String bindMethod, String remark);

    /**
     * 解除绑定
     */
    boolean unbind(Long id);

    /**
     * 解除患者当前绑定
     */
    boolean unbindPatient(Long patientId);

    /**
     * 获取患者当前绑定的医生
     */
    PatientDoctorRelation getActiveDoctor(Long patientId);

    /**
     * 获取医生的所有患者
     */
    List<PatientDoctorRelation> getPatientsByDoctor(Long doctorId);

    /**
     * 获取医生的所有生效中患者
     */
    List<PatientDoctorRelation> getActivePatientsByDoctor(Long doctorId);

    /**
     * 获取患者的绑定历史
     */
    List<PatientDoctorRelation> getBindHistory(Long patientId);

    /**
     * 查询所有绑定关系
     */
    List<PatientDoctorRelation> getList(String patientName, String doctorName, String status);

    /**
     * 统计医生的患者数量
     */
    Long countPatientsByDoctor(Long doctorId);

    /**
     * 根据ID查询
     */
    PatientDoctorRelation getById(Long id);
}