package org.hospital.qmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.mapper.QmgPatientMapper;
import org.hospital.qmg.mapper.QuestionnaireRecordMapper;
import org.hospital.qmg.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private QmgPatientMapper patientMapper;

    @Autowired
    private QuestionnaireRecordMapper recordMapper;

    @Override
    @Cacheable(value = "qmg-stats", key = "'dashboard:' + #doctorId + ':' + #userLevel")
    public Map<String, Object> getStats(Integer doctorId, Integer userLevel) {
        log.info("获取仪表盘统计数据, doctorId={}, userLevel={}", doctorId, userLevel);
        Map<String, Object> stats = new HashMap<>();

        if (userLevel != null && userLevel == 0) {
            stats.put("totalPatients", patientMapper.findAll().size());
        } else if (userLevel != null && userLevel == 1) {
            stats.put("totalPatients", patientMapper.findAllVisibleToAdmin().size());
        } else if (doctorId != null) {
            stats.put("totalPatients", patientMapper.findByDoctorId(doctorId).size());
        } else {
            stats.put("totalPatients", 0);
        }

        stats.put("totalRecords", recordMapper.findAll().size());

        List<Map<String, Object>> dailyStats = recordMapper.countByDayLast7Days();
        stats.put("dailyRecords", dailyStats);

        return stats;
    }

    @Override
    @Cacheable(value = "qmg-stats", key = "'daily'")
    public Map<String, Object> getDailyStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dailyData = recordMapper.countByDayLast7Days();
        result.put("dailyRecords", dailyData);
        return result;
    }

    @Override
    @CacheEvict(value = "qmg-stats", allEntries = true)
    public void refreshStats() {
        log.info("刷新统计数据缓存");
    }

    @Scheduled(fixedRate = 300000)
    @CacheEvict(value = "qmg-stats", allEntries = true)
    public void scheduledRefresh() {
        log.debug("定时刷新统计数据缓存");
    }
}