package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import java.util.List;

@Mapper
public interface PatientDoctorRelationMapper extends BaseMapper<PatientDoctorRelation> {

    // 基础列定义
    String BASE_COLUMNS = "id, patient_id, patient_name, doctor_id, doctor_name, relation_type, status, bind_method, remark, bind_time, unbind_time, create_time";

    @Select("SELECT " + BASE_COLUMNS + " FROM patient_doctor_relation WHERE patient_id = #{patientId} ORDER BY bind_time DESC")
    List<PatientDoctorRelation> selectByPatientId(Long patientId);

    @Select("SELECT " + BASE_COLUMNS + " FROM patient_doctor_relation WHERE patient_id = #{patientId} AND status = 'active' ORDER BY bind_time DESC LIMIT 1")
    PatientDoctorRelation selectActiveByPatientId(Long patientId);

    @Select("SELECT " + BASE_COLUMNS + " FROM patient_doctor_relation WHERE doctor_id = #{doctorId} ORDER BY bind_time DESC")
    List<PatientDoctorRelation> selectByDoctorId(Long doctorId);

    @Select("SELECT " + BASE_COLUMNS + " FROM patient_doctor_relation WHERE doctor_id = #{doctorId} AND status = 'active' ORDER BY bind_time DESC")
    List<PatientDoctorRelation> selectActiveByDoctorId(Long doctorId);

    @Select("SELECT " + BASE_COLUMNS + " FROM patient_doctor_relation WHERE patient_id = #{patientId} AND doctor_id = #{doctorId} LIMIT 1")
    PatientDoctorRelation selectByPatientAndDoctor(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    // 动态 SQL 查询，保留 XML 定义
    List<PatientDoctorRelation> selectRelationList(@Param("patientName") String patientName, @Param("doctorName") String doctorName, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM patient_doctor_relation WHERE doctor_id = #{doctorId} AND status = 'active'")
    Long countByDoctorId(Long doctorId);

    @Update("UPDATE patient_doctor_relation SET status = 'inactive', unbind_time = NOW() WHERE id = #{id}")
    int unbind(Long id);
}