package org.hospital.qmg.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getStats(Integer doctorId, Integer userLevel);

    Map<String, Object> getDailyStats();

    void refreshStats();
}