package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationMapper medicationMapper;

    @Override
    public PageResult<Medication> getList(PageRequest request) {
        List<Medication> list = medicationMapper.selectList(request);
        Long total = medicationMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Medication getById(Long id) {
        return medicationMapper.selectById(id);
    }

    @Override
    public void save(Medication medication) {
        // 如果有 duration 但没有 endDate，自动计算结束日期
        if (medication.getDate() != null && medication.getDuration() != null && medication.getEndDate() == null) {
            medication.setEndDate(calculateEndDate(medication.getDate(), medication.getDuration()));
        }

        if (medication.getId() == null) {
            medicationMapper.insert(medication);
        } else {
            medicationMapper.updateById(medication);
        }
    }

    @Override
    public void delete(Long id) {
        medicationMapper.deleteById(id);
    }

    @Override
    public Long countByDoctorId(Long doctorId) {
        return medicationMapper.selectCountByDoctorId(doctorId);
    }

    @Override
    public PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request) {
        List<Medication> list = medicationMapper.selectListByDoctorId(doctorId, request);
        Long total = medicationMapper.selectCountByDoctorId(doctorId);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    /**
     * 根据 duration 字符串计算结束日期
     * 支持格式：X天、X周、X个月、X月、X年
     */
    private LocalDateTime calculateEndDate(LocalDateTime startDate, String duration) {
        if (startDate == null || duration == null) {
            return null;
        }

        // 解析 duration，如 "1个月"、"3个月"、"7天"、"2周"、"1年"
        Pattern pattern = Pattern.compile("(\\d+)\\s*(天|日|周|个月|月|年)");
        Matcher matcher = pattern.matcher(duration.trim());

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

        // 默认返回一个月
        return startDate.plusMonths(1);
    }
}