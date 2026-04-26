package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.dto.ScheduleDTO;
import org.hospital.neuroimmune.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/neuroimmune/schedule")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 根据日期获取日程
     */
    @GetMapping("/date")
    public Result<ScheduleDTO> getByDate(@RequestParam String date) {
        UserInfo userInfo = getCurrentUser();
        return Result.success(scheduleService.getScheduleByDate(date, userInfo.getRole(), userInfo.getUserId()));
    }

    /**
     * 获取今日日程
     */
    @GetMapping("/today")
    public Result<ScheduleDTO> getToday() {
        UserInfo userInfo = getCurrentUser();
        return Result.success(scheduleService.getTodaySchedule(userInfo.getRole(), userInfo.getUserId()));
    }

    /**
     * 根据日期范围获取日程
     */
    @GetMapping("/range")
    public Result<ScheduleDTO> getByRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        UserInfo userInfo = getCurrentUser();
        return Result.success(scheduleService.getScheduleByRange(startDate, endDate, userInfo.getRole(), userInfo.getUserId()));
    }

    private UserInfo getCurrentUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
}