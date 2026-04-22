package org.hospital.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hospital.common.entity.SessionLog;
import org.hospital.common.mapper.SessionLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 会话审计服务
 */
@Slf4j
@Service
public class SessionAuditService {

    @Autowired
    private SessionLogMapper sessionLogMapper;

    /**
     * 记录登录
     */
    public void recordLogin(Long userId, String role, String module, String ip, String userAgent, String device) {
        SessionLog logEntry = new SessionLog();
        logEntry.setUserId(userId);
        logEntry.setRole(role);
        logEntry.setModule(module);
        logEntry.setLoginTime(LocalDateTime.now());
        logEntry.setIp(ip);
        logEntry.setUserAgent(userAgent);
        logEntry.setDevice(device);
        logEntry.setOperationType("LOGIN");
        sessionLogMapper.insert(logEntry);
    }

    /**
     * 记录登出
     */
    public void recordLogout(Long userId, String role, String module) {
        SessionLog logEntry = new SessionLog();
        logEntry.setUserId(userId);
        logEntry.setRole(role);
        logEntry.setModule(module);
        logEntry.setLoginTime(LocalDateTime.now());
        logEntry.setLogoutTime(LocalDateTime.now());
        logEntry.setOperationType("LOGOUT");
        sessionLogMapper.insert(logEntry);
    }

    /**
     * 记录踢下线
     */
    public void recordKickOffline(Long userId, String role, String module, Long operatorId) {
        SessionLog logEntry = new SessionLog();
        logEntry.setUserId(userId);
        logEntry.setRole(role);
        logEntry.setModule(module);
        logEntry.setLoginTime(LocalDateTime.now());
        logEntry.setLogoutTime(LocalDateTime.now());
        logEntry.setOperationType("KICK_OFFLINE");
        logEntry.setOperatorId(operatorId);
        sessionLogMapper.insert(logEntry);
    }

    /**
     * 查询审计日志
     */
    public Page<SessionLog> queryLogs(String module, String role, String operationType,
                                       LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        Page<SessionLog> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SessionLog> wrapper = new LambdaQueryWrapper<>();

        if (module != null && !module.isEmpty()) {
            wrapper.eq(SessionLog::getModule, module);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(SessionLog::getRole, role);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(SessionLog::getOperationType, operationType);
        }
        if (startTime != null) {
            wrapper.ge(SessionLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SessionLog::getLoginTime, endTime);
        }
        wrapper.orderByDesc(SessionLog::getCreateTime);

        return sessionLogMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取今日登录次数
     */
    public long countTodayLogins() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<SessionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SessionLog::getOperationType, "LOGIN")
               .ge(SessionLog::getLoginTime, todayStart);
        return sessionLogMapper.selectCount(wrapper);
    }

    /**
     * 获取今日踢下线次数
     */
    public long countTodayKickOffline() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<SessionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SessionLog::getOperationType, "KICK_OFFLINE")
               .ge(SessionLog::getCreateTime, todayStart);
        return sessionLogMapper.selectCount(wrapper);
    }

    /**
     * 获取今日操作总数
     */
    public long countTodayOperations() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<SessionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SessionLog::getCreateTime, todayStart);
        return sessionLogMapper.selectCount(wrapper);
    }
}