package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hospital.neuroimmune.dto.ScheduleDTO;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.FollowUpMapper;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private MedicationMapper medicationMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ScheduleDTO getScheduleByDate(String date, String role, Long userId) {
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.setVisit(new ArrayList<>());
        schedule.setFollow(new ArrayList<>());
        schedule.setMedication(new ArrayList<>());

        // 查询随访计划
        List<FollowUp> followUps = getFollowUpsByDate(date, role, userId);
        for (FollowUp fu : followUps) {
            ScheduleDTO.ScheduleItem item = new ScheduleDTO.ScheduleItem();
            item.setId(fu.getId());
            item.setTime(formatTime(fu.getDate()));
            item.setWho("doctor".equals(role) ? fu.getPatientName() : fu.getDoctorName());
            item.setDate(formatDate(fu.getDate()));
            item.setType(fu.getType());
            item.setStatus(fu.getStatus() != null ? String.valueOf(fu.getStatus()) : "0");
            item.setContent(fu.getProject());
            schedule.getFollow().add(item);
        }

        // 查询用药建议
        List<Medication> medications = getMedicationsByDate(date, role, userId);
        for (Medication med : medications) {
            ScheduleDTO.ScheduleItem item = new ScheduleDTO.ScheduleItem();
            item.setId(med.getId());
            item.setTime(med.getFrequency()); // 用药频率作为时间
            item.setWho("doctor".equals(role) ? med.getPatientName() : med.getDoctorName());
            item.setDate(formatDate(med.getDate()));
            // 设置结束日期
            if (med.getEndDate() != null) {
                item.setEndDate(formatDate(med.getEndDate()));
            } else if (med.getDuration() != null && med.getDate() != null) {
                LocalDate startDate = med.getDate().toLocalDate();
                LocalDate endDate = calculateEndDate(startDate, med.getDuration());
                item.setEndDate(endDate.format(DATE_FORMATTER));
            }
            item.setType("medication");
            item.setContent(med.getMedicationName() + " " + (med.getDosageValue() != null ? med.getDosageValue() : "") + (med.getDosageUnit() != null ? med.getDosageUnit() : ""));
            schedule.getMedication().add(item);
        }

        return schedule;
    }

    @Override
    public ScheduleDTO getTodaySchedule(String role, Long userId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return getScheduleByDate(today, role, userId);
    }

    @Override
    public ScheduleDTO getScheduleByRange(String startDate, String endDate, String role, Long userId) {
        // 简化实现，按开始日期查询
        return getScheduleByDate(startDate, role, userId);
    }

    private List<FollowUp> getFollowUpsByDate(String date, String role, Long userId) {
        LocalDate queryDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FollowUp::getDate, startOfDay).lt(FollowUp::getDate, endOfDay);

        if ("doctor".equals(role) && userId != null) {
            wrapper.eq(FollowUp::getDoctorId, userId);
        } else if ("patient".equals(role) && userId != null) {
            wrapper.eq(FollowUp::getPatientId, userId);
        }

        wrapper.orderByAsc(FollowUp::getDate);
        return followUpMapper.selectList(wrapper);
    }

    private List<Medication> getMedicationsByDate(String date, String role, Long userId) {
        LocalDate queryDate = LocalDate.parse(date, DATE_FORMATTER);

        // 获取所有用药建议，然后筛选日期范围内的
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        if ("doctor".equals(role) && userId != null) {
            wrapper.eq(Medication::getDoctorId, userId);
        } else if ("patient".equals(role) && userId != null) {
            wrapper.eq(Medication::getPatientId, userId);
        }
        wrapper.orderByDesc(Medication::getDate);

        List<Medication> allMedications = medicationMapper.selectList(wrapper);

        List<Medication> filtered = allMedications.stream()
                .filter(med -> {
                    // 检查开始日期
                    if (med.getDate() == null) return false;
                    LocalDate startDate = med.getDate().toLocalDate();

                    // 检查结束日期（如果有 duration 但没有 endDate，计算 endDate）
                    LocalDate endDate;
                    if (med.getEndDate() != null) {
                        endDate = med.getEndDate().toLocalDate();
                    } else if (med.getDuration() != null) {
                        endDate = calculateEndDate(startDate, med.getDuration());
                    } else {
                        // 没有 duration 和 endDate，只显示开始日期那天
                        endDate = startDate;
                    }

                    // 查询日期在 [startDate, endDate] 范围内
                    return !queryDate.isBefore(startDate) && !queryDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        return filtered;
    }

    /**
     * 根据 duration 计算结束日期
     */
    private LocalDate calculateEndDate(LocalDate startDate, String duration) {
        if (startDate == null || duration == null) {
            return startDate;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(天|日|周|个月|月|年)");
        java.util.regex.Matcher matcher = pattern.matcher(duration.trim());

        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "天":
                case "日":
                    return startDate.plusDays(amount);
                case "周":
                    return startDate.plusWeeks(amount);
                case "个月":
                case "月":
                    return startDate.plusMonths(amount);
                case "年":
                    return startDate.plusYears(amount);
            }
        }

        return startDate.plusMonths(1);
    }

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "00:00";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }
}