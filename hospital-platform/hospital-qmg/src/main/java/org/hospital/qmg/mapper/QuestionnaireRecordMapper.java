package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.qmg.entity.QuestionnaireRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果Mapper接口
 */
@Mapper
public interface QuestionnaireRecordMapper {
    /**
     * 新增问卷结果
     */
    int insert(QuestionnaireRecord record);

    /**
     * 根据ID查询问卷结果
     */
    QuestionnaireRecord findById(Integer id);

    /**
     * 根据患者ID查询所有问卷结果
     */
    List<QuestionnaireRecord> findByPatientId(Integer patientId);
    
    /**
     * 根据患者ID和医生ID查询所有问卷结果（权限过滤）
     */
    List<QuestionnaireRecord> findByPatientIdAndDoctorId(
        @Param("patientId") Integer patientId,
        @Param("doctorId") Integer doctorId
    );

    /**
     * 根据住院号查询所有问卷结果
     */
    List<QuestionnaireRecord> findByAdmissionNumber(String admissionNumber);

    /**
     * 根据患者ID和日期范围查询问卷结果
     */
    List<QuestionnaireRecord> findByPatientIdAndDateRange(
        @Param("patientId") Integer patientId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 根据患者ID、日期范围和医生ID查询问卷结果（权限过滤）
     */
    List<QuestionnaireRecord> findByPatientIdAndDateRangeAndDoctorId(
        @Param("patientId") Integer patientId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("doctorId") Integer doctorId
    );

    /**
     * 根据住院号和日期范围查询问卷结果
     */
    List<QuestionnaireRecord> findByAdmissionNumberAndDateRange(
        @Param("admissionNumber") String admissionNumber,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询所有问卷结果
     */
    List<QuestionnaireRecord> findAll();
    
    /**
     * 根据医生ID查询所有问卷结果（权限过滤）
     */
    List<QuestionnaireRecord> findByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 根据患者名称和时间范围组合查询（分页）
     */
    List<QuestionnaireRecord> findByPatientNameAndDateRange(
        @Param("patientName") String patientName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );
    
    /**
     * 根据患者名称、时间范围和医生ID组合查询（分页，权限过滤）
     */
    List<QuestionnaireRecord> findByPatientNameAndDateRangeAndDoctorId(
        @Param("patientName") String patientName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("doctorId") Integer doctorId,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    /**
     * 统计根据患者名称和时间范围查询的总数
     */
    int countByPatientNameAndDateRange(
        @Param("patientName") String patientName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 统计根据患者名称、时间范围和医生ID查询的总数（权限过滤）
     */
    int countByPatientNameAndDateRangeAndDoctorId(
        @Param("patientName") String patientName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("doctorId") Integer doctorId
    );

    /**
     * 更新问卷结果
     */
    int update(QuestionnaireRecord record);

    /**
     * 根据ID删除问卷结果
     */
    int deleteById(Integer id);

    /**
     * 统计最近一周每天的问卷数量
     */
    List<Map<String, Object>> countByDayLast7Days();
}
