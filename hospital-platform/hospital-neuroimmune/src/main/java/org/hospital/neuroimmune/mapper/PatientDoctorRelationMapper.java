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

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} AND r.status = 1 AND r.bind_status = 1 " +
            "ORDER BY r.bind_time DESC LIMIT 1")
    PatientDoctorRelation selectConfirmedByPatientId(Long patientId);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.request_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} AND r.status = 1 AND r.bind_status = 0 " +
            "ORDER BY r.request_time DESC LIMIT 1")
    PatientDoctorRelation selectPendingByPatientId(Long patientId);

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id = #{patientId} AND r.status = 1 " +
            "ORDER BY r.bind_time DESC LIMIT 1")
    PatientDoctorRelation selectActiveByPatientId(Long patientId);

    /**
     * 批量查询患者当前的绑定关系（包含bind_status）
     * 返回每个患者最新的绑定关系（status=1），只取每个患者最新一条
     * 使用子查询兼容 MySQL 5.x
     */
    @Select("<script>" +
            "SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.request_time, r.bind_time, r.unbind_time, r.create_time, " +
            "p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.patient_id IN " +
            "<foreach item='id' collection='patientIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND r.status = 1 AND r.is_deleted = 0 " +
            "AND r.request_time = (" +
            "SELECT MAX(r2.request_time) FROM patient_doctor_relation r2 " +
            "WHERE r2.patient_id = r.patient_id AND r2.status = 1 AND r2.is_deleted = 0" +
            ")" +
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

    @Select("SELECT r.id, r.patient_id, r.doctor_id, r.relation_type, r.status, r.bind_status, r.bind_method, " +
            "r.remark, r.bind_time, r.unbind_time, r.create_time, p.name AS patient_name, d.name AS doctor_name " +
            "FROM patient_doctor_relation r " +
            "LEFT JOIN patient p ON r.patient_id = p.id " +
            "LEFT JOIN doctor d ON r.doctor_id = d.id " +
            "WHERE r.doctor_id = #{doctorId} AND r.status = 1 AND r.bind_status = 1 " +
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

    @Select("SELECT CAST(COUNT(*) AS UNSIGNED) " +
            "FROM patient_doctor_relation r " +
            "INNER JOIN patient p ON r.patient_id = p.id " +
            "WHERE r.doctor_id = #{doctorId} AND r.status = 1 AND r.bind_status = 1 " +
            "AND (r.is_deleted = 0 OR r.is_deleted IS NULL) " +
            "AND (p.is_deleted = 0 OR p.is_deleted IS NULL)")
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
     * 批量统计医生的患者数量（只统计已确认绑定的有效患者）
     */
    @Select("<script>" +
            "SELECT r.doctor_id, COUNT(*) as cnt " +
            "FROM patient_doctor_relation r " +
            "INNER JOIN patient p ON r.patient_id = p.id " +
            "WHERE r.status = 1 AND r.bind_status = 1 " +
            "AND (r.is_deleted = 0 OR r.is_deleted IS NULL) " +
            "AND (p.is_deleted = 0 OR p.is_deleted IS NULL) " +
            "AND r.doctor_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY r.doctor_id" +
            "</script>")
    List<Map<String, Object>> countByDoctorIds(@Param("ids") List<Long> doctorIds);

    /**
     * 按绑定状态查询患者ID列表（只取每个患者最新的一条绑定关系）
     * bindStatus: 0-待审核, 1-已确认, 2-已拒绝
     * 使用子查询兼容 MySQL 5.x
     */
    @Select("SELECT DISTINCT r.patient_id FROM patient_doctor_relation r " +
            "WHERE r.status = 1 AND r.is_deleted = 0 AND r.bind_status = #{bindStatus} " +
            "AND r.request_time = (" +
            "SELECT MAX(r2.request_time) FROM patient_doctor_relation r2 " +
            "WHERE r2.patient_id = r.patient_id AND r2.status = 1 AND r2.is_deleted = 0" +
            ")")
    List<Long> selectPatientIdsByBindStatus(@Param("bindStatus") Integer bindStatus);

    /**
     * 查询有绑定关系记录的所有患者ID（用于排除未绑定的患者）
     */
    @Select("SELECT DISTINCT r.patient_id FROM patient_doctor_relation r " +
            "WHERE r.status = 1 AND r.is_deleted = 0")
    List<Long> selectAllPatientIdsWithRelation();
}