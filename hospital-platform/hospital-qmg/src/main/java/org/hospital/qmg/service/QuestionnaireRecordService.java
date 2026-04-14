package org.hospital.qmg.service;

import org.hospital.qmg.entity.QuestionnaireRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果Service接口
 */
public interface QuestionnaireRecordService {
    /**
     * 保存问卷结果
     */
    QuestionnaireRecord save(Map<String, Object> recordData);

    /**
     * 根据ID查询问卷结果
     */
    QuestionnaireRecord findById(Integer id);

    /**
     * 根据患者ID查询所有问卷结果（根据权限过滤）
     */
    List<QuestionnaireRecord> findByPatientId(Integer patientId, Integer currentDoctorId, Integer currentUserLevel);

    /**
     * 根据住院号查询所有问卷结果
     */
    List<QuestionnaireRecord> findByAdmissionNumber(String admissionNumber);

    /**
     * 根据患者ID和日期范围查询问卷结果（根据权限过滤）
     */
    List<QuestionnaireRecord> findByPatientIdAndDateRange(
        Integer patientId, LocalDate startDate, LocalDate endDate, Integer currentDoctorId, Integer currentUserLevel
    );

    /**
     * 根据住院号和日期范围查询问卷结果
     */
    List<QuestionnaireRecord> findByAdmissionNumberAndDateRange(
        String admissionNumber, LocalDate startDate, LocalDate endDate
    );

    /**
     * 查询所有问卷结果（根据权限过滤）
     */
    List<QuestionnaireRecord> findAll(Integer currentDoctorId, Integer currentUserLevel);

    /**
     * 根据患者名称和时间范围组合查询（分页，根据权限过滤）
     */
    List<QuestionnaireRecord> findByPatientNameAndDateRange(
        String patientName, LocalDate startDate, LocalDate endDate, Integer page, Integer pageSize,
        Integer currentDoctorId, Integer currentUserLevel
    );

    /**
     * 统计根据患者名称和时间范围查询的总数（根据权限过滤）
     */
    int countByPatientNameAndDateRange(
        String patientName, LocalDate startDate, LocalDate endDate,
        Integer currentDoctorId, Integer currentUserLevel
    );

    /**
     * 更新问卷结果
     */
    QuestionnaireRecord update(Map<String, Object> recordData);

    /**
     * 根据ID删除问卷结果
     */
    void deleteById(Integer id);

    /**
     * 统计最近一周每天的问卷数量
     */
    List<Map<String, Object>> countByDayLast7Days();
}
