package org.hospital.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.entity.SessionLog;
import org.hospital.admin.service.SessionAuditService;
import org.hospital.common.model.Result;
import org.hospital.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 审计日志 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin/audit")
@CrossOrigin
public class AuditController {

    @Autowired
    private SessionAuditService auditService;

    @PostMapping("/list")
    public Result<PageResult<SessionLog>> getAuditLogs(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? ((Number) params.get("page")).intValue() : 1;
        int size = params.get("pageSize") != null ? ((Number) params.get("pageSize")).intValue() : 20;
        String module = params.get("module") != null ? (String) params.get("module") : null;
        String role = params.get("role") != null ? (String) params.get("role") : null;
        String operationType = params.get("operationType") != null ? (String) params.get("operationType") : null;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (params.get("startTime") != null) {
            startTime = LocalDateTime.parse((String) params.get("startTime"));
        }
        if (params.get("endTime") != null) {
            endTime = LocalDateTime.parse((String) params.get("endTime"));
        }

        Page<SessionLog> pageObj = auditService.queryLogs(module, role, operationType, startTime, endTime, page, size);
        return Result.success(PageResult.of(pageObj.getRecords(), pageObj.getTotal(), page, size));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getAuditStats() {
        long todayOps = auditService.countTodayOperations();
        long loginCount = auditService.countTodayLogins();
        long kickCount = auditService.countTodayKickOffline();
        Map<String, Object> result = new HashMap<>();
        result.put("todayOperations", todayOps);
        result.put("loginCount", loginCount);
        result.put("kickCount", kickCount);
        return Result.success(result);
    }
}