package org.hospital.qmg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.hospital.qmg.entity.QuestionnaireRecord;
import org.hospital.qmg.mapper.QuestionnaireRecordMapper;
import org.hospital.qmg.mapper.PatientMapper;
import org.hospital.qmg.service.QuestionnaireRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果Service实现类
 */
@Slf4j
@Service
public class QuestionnaireRecordServiceImpl implements QuestionnaireRecordService {

    @Autowired
    private QuestionnaireRecordMapper questionnaireRecordMapper;

    @Autowired
    private PatientMapper patientMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public QuestionnaireRecord save(Map<String, Object> recordData) {
        try {
            QuestionnaireRecord record = new QuestionnaireRecord();

            // 解析患者信息（app 端必传 patient 与 admissionNumber）
            @SuppressWarnings("unchecked")
            Map<String, Object> patientMap = (Map<String, Object>) recordData.get("patient");
            if (patientMap == null) {
                throw new RuntimeException("缺少患者信息(patient)");
            }
            Object admissionNumberObj = patientMap.get("admissionNumber");
            String admissionNumber = admissionNumberObj != null ? admissionNumberObj.toString().trim() : null;
            if (admissionNumber == null || admissionNumber.isEmpty()) {
                throw new RuntimeException("患者住院号不能为空");
            }

            var patient = patientMapper.findByAdmissionNumber(admissionNumber);
            if (patient == null) {
                throw new RuntimeException("患者不存在，请确认住院号: " + admissionNumber + " 是否已在系统中登记");
            }
            record.setPatientId(patient.getId());
            record.setAdmissionNumber(admissionNumber);

            // 测评日期
            String assessmentDateStr = (String) recordData.get("assessmentDate");
            if (assessmentDateStr == null || assessmentDateStr.trim().isEmpty()) {
                throw new RuntimeException("测评日期不能为空");
            }
            record.setAssessmentDate(LocalDate.parse(assessmentDateStr.trim()));

            // 选择的选项（转为JSON）
            Object selections = recordData.get("selections");
            record.setSelections(selections != null ? objectMapper.writeValueAsString(selections) : "{}");

            // 得分信息（允许为空，避免 app 端结构不一致导致 NPE）
            @SuppressWarnings("unchecked")
            Map<String, Object> scoreMap = recordData.get("score") != null ? (Map<String, Object>) recordData.get("score") : null;
            if (scoreMap != null) {
                Object itemScores = scoreMap.get("itemScores");
                record.setItemScores(itemScores != null ? objectMapper.writeValueAsString(itemScores) : "{}");
                Object totalScore = scoreMap.get("totalScore");
                record.setTotalScore(totalScore instanceof Number ? ((Number) totalScore).intValue() : 0);
                Object categoryScores = scoreMap.get("categoryScores");
                record.setCategoryScores(categoryScores != null ? objectMapper.writeValueAsString(categoryScores) : "{}");
            } else {
                record.setItemScores("{}");
                record.setTotalScore(0);
                record.setCategoryScores("{}");
            }

            // 医生信息（app 端传入的 doctorId 可能是 Long，需安全转换）
            Object doctorIdObj = recordData.get("doctorId");
            Integer doctorId = doctorIdObj != null ? ((Number) doctorIdObj).intValue() : null;
            String doctorUsername = (String) recordData.get("doctorUsername");
            record.setDoctorId(doctorId);
            record.setDoctorUsername(doctorUsername != null ? doctorUsername : "");

            String modifiedBy = (String) recordData.get("modifiedBy");
            if (modifiedBy == null || modifiedBy.trim().isEmpty()) {
                modifiedBy = doctorUsername != null ? doctorUsername : "doctor";
            }
            record.setModifiedBy(modifiedBy);
            
            // 用户自定义输入数据（如备注、说明等）
            Object userInputData = recordData.get("userInputData");
            if (userInputData != null) {
                if (userInputData instanceof String) {
                    record.setUserInputData((String) userInputData);
                } else {
                    record.setUserInputData(objectMapper.writeValueAsString(userInputData));
                }
            }

            // 时间戳
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            questionnaireRecordMapper.insert(record);

            log.info("保存问卷结果成功，ID: {}, 患者ID: {}, 总分: {}", 
                record.getId(), record.getPatientId(), record.getTotalScore());

            return record;

        } catch (Exception e) {
            log.error("保存问卷结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存问卷结果失败: " + e.getMessage(), e);
        }
    }

    @Override
    public QuestionnaireRecord findById(Integer id) {
        return questionnaireRecordMapper.findById(id);
    }

    @Override
    public List<QuestionnaireRecord> findByPatientId(Integer patientId, Integer currentDoctorId, Integer currentUserLevel) {
        // 权限等级0或1可以查看所有记录
        if (currentUserLevel != null && (currentUserLevel == 0 || currentUserLevel == 1)) {
            return questionnaireRecordMapper.findByPatientId(patientId);
        }
        
        // 普通医生只能查看自己对应的患者的记录
        if (currentDoctorId != null) {
            return questionnaireRecordMapper.findByPatientIdAndDoctorId(patientId, currentDoctorId);
        }
        
        // 如果没有医生ID，返回空列表
        return Collections.emptyList();
    }

    @Override
    public List<QuestionnaireRecord> findByAdmissionNumber(String admissionNumber) {
        return questionnaireRecordMapper.findByAdmissionNumber(admissionNumber);
    }

    @Override
    public List<QuestionnaireRecord> findByPatientIdAndDateRange(
        Integer patientId, LocalDate startDate, LocalDate endDate, Integer currentDoctorId, Integer currentUserLevel
    ) {
        // 权限等级0或1可以查看所有记录
        if (currentUserLevel != null && (currentUserLevel == 0 || currentUserLevel == 1)) {
            return questionnaireRecordMapper.findByPatientIdAndDateRange(patientId, startDate, endDate);
        }
        
        // 普通医生只能查看自己对应的患者的记录
        if (currentDoctorId != null) {
            return questionnaireRecordMapper.findByPatientIdAndDateRangeAndDoctorId(patientId, startDate, endDate, currentDoctorId);
        }
        
        // 如果没有医生ID，返回空列表
        return Collections.emptyList();
    }

    @Override
    public List<QuestionnaireRecord> findByAdmissionNumberAndDateRange(
        String admissionNumber, LocalDate startDate, LocalDate endDate
    ) {
        return questionnaireRecordMapper.findByAdmissionNumberAndDateRange(admissionNumber, startDate, endDate);
    }

    @Override
    public List<QuestionnaireRecord> findAll(Integer currentDoctorId, Integer currentUserLevel) {
        // 权限等级0或1可以查看所有记录
        if (currentUserLevel != null && (currentUserLevel == 0 || currentUserLevel == 1)) {
            return questionnaireRecordMapper.findAll();
        }
        
        // 普通医生只能查看自己对应的患者的记录
        if (currentDoctorId != null) {
            return questionnaireRecordMapper.findByDoctorId(currentDoctorId);
        }
        
        // 如果没有医生ID，返回空列表
        return Collections.emptyList();
    }

    @Override
    public List<QuestionnaireRecord> findByPatientNameAndDateRange(
        String patientName, LocalDate startDate, LocalDate endDate, Integer page, Integer pageSize,
        Integer currentDoctorId, Integer currentUserLevel
    ) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        Integer offset = (page - 1) * pageSize;
        
        // 权限等级0或1可以查看所有记录
        if (currentUserLevel != null && (currentUserLevel == 0 || currentUserLevel == 1)) {
            return questionnaireRecordMapper.findByPatientNameAndDateRange(
                patientName, startDate, endDate, offset, pageSize
            );
        }
        
        // 普通医生只能查看自己对应的患者的记录
        if (currentDoctorId != null) {
            return questionnaireRecordMapper.findByPatientNameAndDateRangeAndDoctorId(
                patientName, startDate, endDate, currentDoctorId, offset, pageSize
            );
        }
        
        // 如果没有医生ID，返回空列表
        return Collections.emptyList();
    }

    @Override
    public int countByPatientNameAndDateRange(
        String patientName, LocalDate startDate, LocalDate endDate,
        Integer currentDoctorId, Integer currentUserLevel
    ) {
        // 权限等级0或1可以查看所有记录
        if (currentUserLevel != null && (currentUserLevel == 0 || currentUserLevel == 1)) {
            return questionnaireRecordMapper.countByPatientNameAndDateRange(
                patientName, startDate, endDate
            );
        }
        
        // 普通医生只能查看自己对应的患者的记录
        if (currentDoctorId != null) {
            return questionnaireRecordMapper.countByPatientNameAndDateRangeAndDoctorId(
                patientName, startDate, endDate, currentDoctorId
            );
        }
        
        // 如果没有医生ID，返回0
        return 0;
    }

    @Override
    @Transactional
    public QuestionnaireRecord update(Map<String, Object> recordData) {
        try {
            // 获取记录ID
            Integer id = ((Number) recordData.get("id")).intValue();
            QuestionnaireRecord record = questionnaireRecordMapper.findById(id);
            if (record == null) {
                throw new RuntimeException("问卷结果不存在，ID: " + id);
            }

            // 解析患者信息
            @SuppressWarnings("unchecked")
            Map<String, Object> patientMap = (Map<String, Object>) recordData.get("patient");
            String admissionNumber = (String) patientMap.get("admissionNumber");

            // 根据住院号查找患者ID
            var patient = patientMapper.findByAdmissionNumber(admissionNumber);
            if (patient == null) {
                throw new RuntimeException("患者不存在，住院号: " + admissionNumber);
            }
            record.setPatientId(patient.getId());
            record.setAdmissionNumber(admissionNumber);

            // 测评日期
            String assessmentDateStr = (String) recordData.get("assessmentDate");
            record.setAssessmentDate(LocalDate.parse(assessmentDateStr));

            // 选择的选项（转为JSON）
            Object selections = recordData.get("selections");
            record.setSelections(objectMapper.writeValueAsString(selections));

            // 得分信息
            @SuppressWarnings("unchecked")
            Map<String, Object> scoreMap = (Map<String, Object>) recordData.get("score");
            
            // 各项得分
            Object itemScores = scoreMap.get("itemScores");
            record.setItemScores(objectMapper.writeValueAsString(itemScores));
            
            // 总分
            Object totalScore = scoreMap.get("totalScore");
            if (totalScore instanceof Number) {
                record.setTotalScore(((Number) totalScore).intValue());
            } else {
                record.setTotalScore(0);
            }
            
            // 分类得分
            Object categoryScores = scoreMap.get("categoryScores");
            record.setCategoryScores(objectMapper.writeValueAsString(categoryScores));

            // 医生信息（如果有）
            Integer doctorId = (Integer) recordData.get("doctorId");
            String doctorUsername = (String) recordData.get("doctorUsername");
            record.setDoctorId(doctorId);
            record.setDoctorUsername(doctorUsername);
            
            // 修改人员（记录最后修改者）
            String modifiedBy = (String) recordData.get("modifiedBy");
            if (modifiedBy == null || modifiedBy.trim().isEmpty()) {
                modifiedBy = doctorUsername; // 如果没有指定，使用医生用户名
            }
            record.setModifiedBy(modifiedBy);
            
            // 用户自定义输入数据（如备注、说明等）
            Object userInputData = recordData.get("userInputData");
            if (userInputData != null) {
                if (userInputData instanceof String) {
                    record.setUserInputData((String) userInputData);
                } else {
                    record.setUserInputData(objectMapper.writeValueAsString(userInputData));
                }
            }

            // 更新时间
            record.setUpdateTime(LocalDateTime.now());

            // 更新到数据库
            questionnaireRecordMapper.update(record);

            log.info("更新问卷结果成功，ID: {}, 患者ID: {}, 总分: {}", 
                record.getId(), record.getPatientId(), record.getTotalScore());

            return record;

        } catch (Exception e) {
            log.error("更新问卷结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新问卷结果失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        questionnaireRecordMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> countByDayLast7Days() {
        return questionnaireRecordMapper.countByDayLast7Days();
    }
}
