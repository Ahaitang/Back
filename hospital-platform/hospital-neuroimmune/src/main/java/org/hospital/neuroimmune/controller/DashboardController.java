package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.security.SecurityContextHelper;
import org.hospital.neuroimmune.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 仪表盘控制器
 * 已重构：使用 DashboardService 替代在 Controller 中进行数据聚合
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        String role = SecurityContextHelper.getCurrentRole();
        Map<String, Object> stats = dashboardService.getStatsByRole(userId, role);
        return Result.success(stats);
    }
}