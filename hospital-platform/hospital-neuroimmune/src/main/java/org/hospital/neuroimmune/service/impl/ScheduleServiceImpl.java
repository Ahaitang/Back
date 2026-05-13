package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hospital.neuroimmune.dto.ScheduleDTO;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.mapper.FollowUpMapper;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private MedicationMapper medicationMapper;

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

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

        // 批量获取医生和患者信息
        fillFollowUpNames(followUps);

        for (FollowUp fu : followUps) {
            ScheduleDTO.ScheduleItem item = new ScheduleDTO.ScheduleItem();
            item.setId(fu.getId());
            // 时间显示：门诊显示时段，住院显示具体时间
            if (fu.getOutpatientCycleType() != null) {
                item.setTime(formatTimeSlot(fu.getOutpatientTimeSlot()));
            } else {
                item.setTime(formatTime(fu.getHospitalizationTime()));
            }
            item.setWho("doctor".equals(role) ? fu.getPatientName() : fu.getDoctorName());
            item.setDate(formatDate(fu.getHospitalizationTime()));
            item.setType(fu.getFollowUpExamTypeName());
            item.setStatus(fu.getStatus() != null ? String.valueOf(fu.getStatus()) : "0");
            item.setContent(fu.getExaminationItems());
            schedule.getFollow().add(item);
        }

        // 查询用药建议
        List<Medication> medications = getMedicationsByDate(date, role, userId);

        // 批量获取医生和患者信息
        fillMedicationNames(medications);

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

    /**
     * 批量填充随访记录的医生和患者名称
     */
    private void fillFollowUpNames(List<FollowUp> followUps) {
        if (followUps == null || followUps.isEmpty()) return;

        // 收集所有 doctorId 和 patientId
        Set<Long> doctorIds = followUps.stream()
                .map(FollowUp::getDoctorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> patientIds = followUps.stream()
                .map(FollowUp::getPatientId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 批量查询医生
        Map<Long, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<Doctor> doctors = doctorMapper.selectBatchIds(doctorIds);
            doctorNameMap = doctors.stream()
                    .collect(Collectors.toMap(Doctor::getId, Doctor::getName, (a, b) -> a));
        }

        // 批量查询患者
        Map<Long, String> patientNameMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            List<Patient> patients = patientMapper.selectBatchIds(patientIds);
            patientNameMap = patients.stream()
                    .collect(Collectors.toMap(Patient::getId, Patient::getName, (a, b) -> a));
        }

        // 填充名称
        for (FollowUp fu : followUps) {
            if (fu.getDoctorId() != null) {
                fu.setDoctorName(doctorNameMap.getOrDefault(fu.getDoctorId(), "医生"));
            }
            if (fu.getPatientId() != null) {
                fu.setPatientName(patientNameMap.getOrDefault(fu.getPatientId(), "患者"));
            }
        }
    }

    /**
     * 批量填充用药记录的医生和患者名称
     */
    private void fillMedicationNames(List<Medication> medications) {
        if (medications == null || medications.isEmpty()) return;

        // 收集所有 doctorId 和 patientId
        Set<Long> doctorIds = medications.stream()
                .map(Medication::getDoctorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> patientIds = medications.stream()
                .map(Medication::getPatientId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 批量查询医生
        Map<Long, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<Doctor> doctors = doctorMapper.selectBatchIds(doctorIds);
            doctorNameMap = doctors.stream()
                    .collect(Collectors.toMap(Doctor::getId, Doctor::getName, (a, b) -> a));
        }

        // 批量查询患者
        Map<Long, String> patientNameMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            List<Patient> patients = patientMapper.selectBatchIds(patientIds);
            patientNameMap = patients.stream()
                    .collect(Collectors.toMap(Patient::getId, Patient::getName, (a, b) -> a));
        }

        // 填充名称
        for (Medication med : medications) {
            if (med.getDoctorId() != null) {
                med.setDoctorName(doctorNameMap.getOrDefault(med.getDoctorId(), "医生"));
            }
            if (med.getPatientId() != null) {
                med.setPatientName(patientNameMap.getOrDefault(med.getPatientId(), "患者"));
            }
        }
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

    /**
     * 根据日期查询随访计划
     * 同时考虑住院时间和门诊周期
     */
    private List<FollowUp> getFollowUpsByDate(String date, String role, Long userId) {
        LocalDate queryDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.plusDays(1).atStartOfDay();

        // 先获取所有随访记录
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        if ("doctor".equals(role) && userId != null) {
            wrapper.eq(FollowUp::getDoctorId, userId);
        } else if ("patient".equals(role) && userId != null) {
            wrapper.eq(FollowUp::getPatientId, userId);
        }
        wrapper.orderByAsc(FollowUp::getCreateTime);

        List<FollowUp> allFollowUps = followUpMapper.selectList(wrapper);

        // 筛选匹配当天日期的记录（住院时间或门诊周期）
        return allFollowUps.stream()
                .filter(fu -> isFollowUpOnDate(fu, queryDate, startOfDay, endOfDay))
                .collect(Collectors.toList());
    }

    /**
     * 判断随访是否在指定日期
     */
    private boolean isFollowUpOnDate(FollowUp fu, LocalDate queryDate, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        // 住院时间匹配
        if (fu.getHospitalizationTime() != null) {
            LocalDateTime hospTime = fu.getHospitalizationTime();
            if (!hospTime.isBefore(startOfDay) && hospTime.isBefore(endOfDay)) {
                return true;
            }
        }

        // 门诊周期匹配
        if (fu.getOutpatientCycleType() != null && fu.getOutpatientCycleValue() != null) {
            try {
                int cycleValue = Integer.parseInt(fu.getOutpatientCycleValue());
                switch (fu.getOutpatientCycleType()) {
                    case "weekly":
                        // 查询日期是周几（Java中周一=1，周日=7）
                        int dayOfWeek = queryDate.getDayOfWeek().getValue();
                        return dayOfWeek == cycleValue;
                    case "monthly":
                        // 查询日期是几号
                        return queryDate.getDayOfMonth() == cycleValue;
                    case "quarterly":
                        // 每季度几号
                        return queryDate.getDayOfMonth() == cycleValue;
                    default:
                        return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * 格式化时间段
     */
    private String formatTimeSlot(String timeSlot) {
        if (timeSlot == null) return "待定";
        switch (timeSlot) {
            case "morning":
                return "上午";
            case "afternoon":
                return "下午";
            case "evening":
                return "晚间";
            default:
                return "待定";
        }
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