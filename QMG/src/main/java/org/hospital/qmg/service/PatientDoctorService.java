package org.hospital.qmg.service;

import java.util.List;

/**
 * 患者-医生对应Service接口
 */
public interface PatientDoctorService {
    
    /**
     * 添加患者-医生对应关系
     */
    void addMapping(Integer patientId, Integer doctorId);
    
    /**
     * 删除患者-医生对应关系
     */
    void removeMapping(Integer patientId, Integer doctorId);
    
    /**
     * 根据患者ID查询对应的医生ID列表
     */
    List<Integer> findDoctorIdsByPatientId(Integer patientId);

    /**
     * 根据患者ID查询创建人医生ID（patient_doctor 中该患者最早一条的 doctor_id）
     */
    Integer getCreatorDoctorIdByPatientId(Integer patientId);

    /**
     * 根据医生ID查询对应的患者ID列表
     */
    List<Integer> findPatientIdsByDoctorId(Integer doctorId);
    
    /**
     * 检查患者和医生是否存在对应关系
     */
    boolean existsMapping(Integer patientId, Integer doctorId);
    
    /**
     * 删除患者的所有对应关系
     */
    void deleteByPatientId(Integer patientId);
    
    /**
     * 删除医生的所有对应关系
     */
    void deleteByDoctorId(Integer doctorId);
}
