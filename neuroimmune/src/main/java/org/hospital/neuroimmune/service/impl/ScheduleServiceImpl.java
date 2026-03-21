package org.hospital.neuroimmune.service.impl;

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
            item.setTime(formatTimeRange(fu.getDate()));
            item.setWho("doctor".equals(role) ? fu.getPatientName() : fu.getDoctorName());
            item.setDate(formatDate(fu.getDate()));
            item.setType(fu.getType());
            item.setStatus(fu.getStatus());
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
            item.setType("medication");
            item.setContent(med.getMedicationName() + " " + med.getDosage() + med.getUnit());
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
        List<FollowUp> allFollowUps = followUpMapper.selectList(createDateRequest(date));

        if ("doctor".equals(role) && userId != null) {
            // 医生只看自己的随访
            return allFollowUps.stream()
                    .filter(fu -> userId.equals(fu.getDoctorId()))
                    .collect(Collectors.toList());
        } else if ("patient".equals(role) && userId != null) {
            // 患者只看自己的随访
            return allFollowUps.stream()
                    .filter(fu -> userId.equals(fu.getPatientId()))
                    .collect(Collectors.toList());
        }
        return allFollowUps;
    }

    private List<Medication> getMedicationsByDate(String date, String role, Long userId) {
        List<Medication> allMedications = medicationMapper.selectList(createDateRequest(date));

        if ("doctor".equals(role) && userId != null) {
            return allMedications.stream()
                    .filter(med -> userId.equals(med.getDoctorId()))
                    .collect(Collectors.toList());
        } else if ("patient".equals(role) && userId != null) {
            return allMedications.stream()
                    .filter(med -> userId.equals(med.getPatientId()))
                    .collect(Collectors.toList());
        }
        return allMedications;
    }

    private org.hospital.neuroimmune.dto.PageRequest createDateRequest(String date) {
        org.hospital.neuroimmune.dto.PageRequest request = new org.hospital.neuroimmune.dto.PageRequest();
        request.setStartDate(date);
        request.setEndDate(date);
        request.setPageSize(100); // 足够大的页面大小
        return request;
    }

    private String formatTimeRange(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }
}