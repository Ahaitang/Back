package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.ScheduleDTO;
import org.hospital.neuroimmune.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 根据日期获取日程
     */
    @GetMapping("/date")
    public Result<ScheduleDTO> getByDate(
            @RequestParam String date,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(scheduleService.getScheduleByDate(date, role, userId));
    }

    /**
     * 获取今日日程
     */
    @GetMapping("/today")
    public Result<ScheduleDTO> getToday(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(scheduleService.getTodaySchedule(role, userId));
    }

    /**
     * 根据日期范围获取日程
     */
    @GetMapping("/range")
    public Result<ScheduleDTO> getByRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(scheduleService.getScheduleByRange(startDate, endDate, role, userId));
    }
}