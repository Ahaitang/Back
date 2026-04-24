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

    // 不再使用 BASE_COLUMNS，改为 JOIN 查询获取名称

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} " +
            "ORDER BY r.bind_time DESC")
    List<PatientDoctorRelation> selectByPatientId(Long patientId);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} AND r.status = 1 " +
            "ORDER BY r.bind_time DESC LIMIT 1")
    PatientDoctorRelation selectActiveByPatientId(Long patientId);

    /**
     * 批量查询患者当前生效的主治医生
     */
    @Select("<script>" +
            "SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id IN " +
            "<foreach item='id' collection='patientIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND r.status = 1 AND r.relation_type = 'primary'" +
            "</script>")
    List<PatientDoctorRelation> selectBatchActiveByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.doctor_id = #{doctorId} " +
            "ORDER BY r.bind_time DESC")
    List<PatientDoctorRelation> selectByDoctorId(Long doctorId);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.doctor_id = #{doctorId} AND r.status = 1 " +
            "ORDER BY r.bind_time DESC")
    List<PatientDoctorRelation> selectActiveByDoctorId(Long doctorId);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} AND r.doctor_id = #{doctorId} LIMIT 1")
    PatientDoctorRelation selectByPatientAndDoctor(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    // 动态 SQL 查询，保留 XML 定义
    List<PatientDoctorRelation> selectRelationList(@Param("patientName") String patientName, @Param("doctorName") String doctorName, @Param("status") String status);

    @Select("SELECT CAST(COUNT(*) AS UNSIGNED) FROM patient_doctor_relation WHERE doctor_id = #{doctorId} AND status = 1")
    Long countByDoctorId(Long doctorId);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE id = #{id}")
    int unbind(Long id);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE patient_id = #{patientId} AND status = 1")
    int unbindAllByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE doctor_id = #{doctorId} AND status = 1")
    int unbindAllByDoctorId(@Param("doctorId") Long doctorId);
}