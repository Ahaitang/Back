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

    @GetMapping("/list")
    public Result<PageResult<SessionLog>> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        Page<SessionLog> pageObj = auditService.queryLogs(module, role, operationType, startTime, endTime, page, pageSize);
        return Result.success(PageResult.of(pageObj.getRecords(), pageObj.getTotal(), page, pageSize));
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