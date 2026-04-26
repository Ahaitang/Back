package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import java.util.List;
import java.util.Map;

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

    /**
     * 按医生ID和绑定状态查询
     */
    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.request_time, r.confirm_time, r.bind_time, r.unbind_time, r.create_time, " +
            "p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.doctor_id = #{doctorId} AND r.bind_status = #{bindStatus} AND r.is_deleted = 0 " +
            "ORDER BY r.request_time DESC")
    List<PatientDoctorRelation> selectByDoctorIdAndBindStatus(
        @Param("doctorId") Long doctorId,
        @Param("bindStatus") Integer bindStatus);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE id = #{id}")
    int unbind(Long id);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE patient_id = #{patientId} AND status = 1")
    int unbindAllByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE patient_doctor_relation SET status = 0, unbind_time = NOW() WHERE doctor_id = #{doctorId} AND status = 1")
    int unbindAllByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 批量统计医生的患者数量
     */
    @Select("<script>" +
            "SELECT doctor_id, COUNT(*) as cnt FROM patient_doctor_relation " +
            "WHERE status = 1 AND doctor_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY doctor_id" +
            "</script>")
    List<Map<String, Object>> countByDoctorIds(@Param("ids") List<Long> doctorIds);
}