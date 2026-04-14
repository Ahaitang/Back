package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.dto.ScheduleDTO;

/**
 * 日程服务接口
 */
public interface ScheduleService {

    /**
     * 根据日期获取日程
     * @param date 日期 (yyyy-MM-dd)
     * @param role 用户角色
     * @param userId 用户ID
     * @return 日程数据
     */
    ScheduleDTO getScheduleByDate(String date, String role, Long userId);

    /**
     * 获取今日日程
     * @param role 用户角色
     * @param userId 用户ID
     * @return 日程数据
     */
    ScheduleDTO getTodaySchedule(String role, Long userId);

    /**
     * 根据日期范围获取日程
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param role 用户角色
     * @param userId 用户ID
     * @return 日程数据
     */
    ScheduleDTO getScheduleByRange(String startDate, String endDate, String role, Long userId);
}