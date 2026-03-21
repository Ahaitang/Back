package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.qmg.entity.PatientDoctor;
import java.util.List;

/**
 * 患者-医生对应Mapper
 */
@Mapper
public interface PatientDoctorMapper {
    
    /**
     * 新增患者-医生对应关系
     */
    int insert(PatientDoctor patientDoctor);
    
    /**
     * 根据患者ID查询对应的医生ID列表
     */
    List<Integer> findDoctorIdsByPatientId(@Param("patientId") Integer patientId);

    /**
     * 根据患者ID查询创建人医生ID（patient_doctor 中该患者最早一条记录的 doctor_id）
     */
    Integer findCreatorDoctorIdByPatientId(@Param("patientId") Integer patientId);
    
    /**
     * 根据医生ID查询对应的患者ID列表
     */
    List<Integer> findPatientIdsByDoctorId(@Param("doctorId") Integer doctorId);
    
    /**
     * 根据患者ID和医生ID查询对应关系
     */
    PatientDoctor findByPatientIdAndDoctorId(@Param("patientId") Integer patientId, @Param("doctorId") Integer doctorId);
    
    /**
     * 删除患者-医生对应关系
     */
    int deleteByPatientIdAndDoctorId(@Param("patientId") Integer patientId, @Param("doctorId") Integer doctorId);
    
    /**
     * 删除患者的所有对应关系
     */
    int deleteByPatientId(@Param("patientId") Integer patientId);
    
    /**
     * 删除医生的所有对应关系
     */
    int deleteByDoctorId(@Param("doctorId") Integer doctorId);
}
