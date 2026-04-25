package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;

import java.util.List;
import java.util.Map;

/**
 * 实体查询服务
 * 用于跨模块获取患者/医生信息，避免直接操作其他模块的 Mapper
 */
public interface EntityQueryService {

    /**
     * 批量获取患者实体
     * @param ids 患者ID列表
     * @return Map<patientId, Patient>
     */
    Map<Long, Patient> batchGetPatients(List<Long> ids);

    /**
     * 批量获取医生实体
     * @param ids 医生ID列表
     * @return Map<doctorId, Doctor>
     */
    Map<Long, Doctor> batchGetDoctors(List<Long> ids);

    /**
     * 获取单个患者
     * @param id 患者ID
     * @return 患者实体
     */
    Patient getPatient(Long id);

    /**
     * 获取单个医生
     * @param id 医生ID
     * @return 医生实体
     */
    Doctor getDoctor(Long id);

    /**
     * 批量获取患者姓名
     * @param ids 患者ID列表
     * @return Map<patientId, patientName>
     */
    Map<Long, String> batchGetPatientNames(List<Long> ids);

    /**
     * 批量获取医生姓名
     * @param ids 医生ID列表
     * @return Map<doctorId, doctorName>
     */
    Map<Long, String> batchGetDoctorNames(List<Long> ids);

    /**
     * 获取患者姓名
     * @param id 患者ID
     * @return 患者姓名
     */
    String getPatientName(Long id);

    /**
     * 获取医生姓名
     * @param id 医生ID
     * @return 医生姓名
     */
    String getDoctorName(Long id);
}