package org.hospital.qmg.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.QuestionnaireRecord;
import org.hospital.common.model.Result;
import org.hospital.qmg.service.QuestionnaireRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果Controller
 * 全部使用POST请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/qmg/questionnaire")
@CrossOrigin(origins = "*")
public class QuestionnaireRecordController {

    @Autowired
    private QuestionnaireRecordService questionnaireRecordService;

    /**
     * 保存问卷结果
     */
    @PostMapping("/save")
    public Result save(@RequestBody Map<String, Object> recordData) {
        log.info("保存问卷结果请求");
        try {
            QuestionnaireRecord record = questionnaireRecordService.save(recordData);
            return Result.success(record);
        } catch (Exception e) {
            log.error("保存问卷结果失败: {}", e.getMessage(), e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询问卷结果
     */
    @PostMapping("/getById")
    public Result getById(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        log.info("根据ID查询问卷结果: {}", id);
        QuestionnaireRecord record = questionnaireRecordService.findById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.error("问卷结果不存在");
    }

    /**
     * 根据患者ID查询问卷结果（支持日期范围筛选，根据权限过滤）
     * 如果 startDate 和 endDate 为空，则查询所有记录
     */
    @PostMapping("/getByPatientId")
    public Result getByPatientId(@RequestBody Map<String, Object> params) {
        Integer patientId = params.get("patientId") != null 
            ? ((Number) params.get("patientId")).intValue() : null;
        String startDateStr = (String) params.get("startDate");
        String endDateStr = (String) params.get("endDate");
        Integer currentDoctorId = params.get("currentDoctorId") != null 
            ? ((Number) params.get("currentDoctorId")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null 
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        if ("mine".equals(params.get("scope"))) {
            currentUserLevel = null;
        }
        
        LocalDate startDate = startDateStr != null && !startDateStr.trim().isEmpty() 
            ? LocalDate.parse(startDateStr) : null;
        LocalDate endDate = endDateStr != null && !endDateStr.trim().isEmpty() 
            ? LocalDate.parse(endDateStr) : null;
        
        log.info("根据患者ID查询问卷结果: patientId={}, startDate={}, endDate={}, currentDoctorId={}, currentUserLevel={}", 
            patientId, startDate, endDate, currentDoctorId, currentUserLevel);
        
        List<QuestionnaireRecord> records;
        if (startDate != null || endDate != null) {
            records = questionnaireRecordService.findByPatientIdAndDateRange(
                patientId, startDate, endDate, currentDoctorId, currentUserLevel);
        } else {
            records = questionnaireRecordService.findByPatientId(patientId, currentDoctorId, currentUserLevel);
        }
        return Result.success(records);
    }


    /**
     * 查询问卷结果（支持分页和条件筛选）
     * 如果 patientName、startDate、endDate 都为空，则查询所有记录
     * 如果 page 和 pageSize 为空，则返回所有记录（不分页）
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        
        String patientName = (String) params.get("patientName");
        String startDateStr = (String) params.get("startDate");
        String endDateStr = (String) params.get("endDate");
        Integer page = params.get("page") != null ? ((Number) params.get("page")).intValue() : null;
        Integer pageSize = params.get("pageSize") != null ? ((Number) params.get("pageSize")).intValue() : null;
        Integer currentDoctorId = params.get("currentDoctorId") != null 
            ? ((Number) params.get("currentDoctorId")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null 
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        // App 端传 scope=mine 时只按当前医生过滤，不看权限等级，避免看到其他用户的记录
        if ("mine".equals(params.get("scope"))) {
            currentUserLevel = null;
        }
        
        LocalDate startDate = startDateStr != null && !startDateStr.trim().isEmpty() 
            ? LocalDate.parse(startDateStr) : null;
        LocalDate endDate = endDateStr != null && !endDateStr.trim().isEmpty() 
            ? LocalDate.parse(endDateStr) : null;
        
        // 如果patientName为空字符串，设置为null
        if (patientName != null && patientName.trim().isEmpty()) {
            patientName = null;
        }
        
        // 如果有筛选条件或需要分页，使用搜索接口
        if (patientName != null || startDate != null || endDate != null || (page != null && pageSize != null)) {
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 20;
            
            log.info("分页查询问卷结果: patientName={}, startDate={}, endDate={}, page={}, pageSize={}, currentDoctorId={}, currentUserLevel={}", 
                patientName, startDate, endDate, page, pageSize, currentDoctorId, currentUserLevel);
            
            List<QuestionnaireRecord> records = questionnaireRecordService.findByPatientNameAndDateRange(
                patientName, startDate, endDate, page, pageSize, currentDoctorId, currentUserLevel
            );
            int total = questionnaireRecordService.countByPatientNameAndDateRange(
                patientName, startDate, endDate, currentDoctorId, currentUserLevel
            );
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("hasMore", (page * pageSize) < total);
            
            return Result.success(result);
        } else {
            // 无条件且不分页，返回所有记录（根据权限过滤）
            log.info("查询所有问卷结果，currentDoctorId={}, currentUserLevel={}", currentDoctorId, currentUserLevel);
            List<QuestionnaireRecord> records = questionnaireRecordService.findAll(currentDoctorId, currentUserLevel);
            return Result.success(records);
        }
    }


    /**
     * 统计最近一周每天的问卷数量
     */
    @PostMapping("/countByDayLast7Days")
    public Result countByDayLast7Days() {
        log.info("统计最近一周每天的问卷数量");
        try {
            List<Map<String, Object>> statistics = questionnaireRecordService.countByDayLast7Days();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("统计失败: {}", e.getMessage(), e);
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    /**
     * 更新问卷结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Map<String, Object> recordData) {
        Integer id = ((Number) recordData.get("id")).intValue();
        String modifiedBy = (String) recordData.get("modifiedBy");
        log.info("更新问卷结果: {}, 修改人员: {}", id, modifiedBy);
        
        // 如果没有指定修改人员，尝试从请求中获取当前用户名
        if (modifiedBy == null || modifiedBy.trim().isEmpty()) {
            modifiedBy = (String) recordData.get("currentUsername");
        }
        recordData.put("modifiedBy", modifiedBy);
        
        try {
            QuestionnaireRecord updated = questionnaireRecordService.update(recordData);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新问卷结果失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除问卷结果
     */
    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        log.info("删除问卷结果: {}", id);
        try {
            questionnaireRecordService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除问卷结果失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
