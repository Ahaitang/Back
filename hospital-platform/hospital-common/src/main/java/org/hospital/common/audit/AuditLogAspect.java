package org.hospital.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hospital.common.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOG");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime operationTime = LocalDateTime.now();

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ipAddress = request != null ? getClientIp(request) : "unknown";
        String requestUri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";

        // 获取用户信息
        UserInfo userInfo = getCurrentUser();
        Long userId = userInfo != null ? userInfo.getUserId() : null;
        String username = userInfo != null ? userInfo.getUsername() : "anonymous";
        String role = userInfo != null ? userInfo.getRole() : "unknown";

        // 设置 MDC
        MDC.put("userId", userId != null ? userId.toString() : "");
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String params = auditLog.logParams() ? serializeParams(args, signature, auditLog.sensitiveFields()) : "masked";

        // 执行结果
        Object result = null;
        String status = "success";
        String errorMessage = "";

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = "failure";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String resultStr = auditLog.logResult() ? serializeResult(result, auditLog.sensitiveFields()) : "masked";

            // 构建审计日志
            AuditLogRecord record = new AuditLogRecord(
                    operationTime.format(FORMATTER),
                    userId,
                    username,
                    role,
                    auditLog.operation().name(),
                    auditLog.module(),
                    auditLog.description(),
                    requestUri,
                    method,
                    ipAddress,
                    params,
                    resultStr,
                    status,
                    errorMessage,
                    duration
            );

            // 写入审计日志文件
            AUDIT_LOGGER.info(record.toLogString());

            // 清除 MDC
            MDC.remove("userId");
            MDC.remove("requestId");
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            return (UserInfo) authentication.getPrincipal();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String serializeParams(Object[] args, MethodSignature signature, String[] sensitiveFields) {
        try {
            if (args == null || args.length == 0) {
                return "none";
            }

            String[] paramNames = signature.getParameterNames();
            Map<String, Object> paramsMap = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                // 过滤掉 HttpServletRequest/HttpResponse 等不可序列化的参数
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    continue;
                }
                String paramName = paramNames != null && i < paramNames.length ? paramNames[i] : "arg" + i;
                paramsMap.put(paramName, maskSensitiveFields(arg, sensitiveFields));
            }

            return objectMapper.writeValueAsString(paramsMap);
        } catch (Exception e) {
            return "serialization_error: " + e.getMessage();
        }
    }

    private String serializeResult(Object result, String[] sensitiveFields) {
        try {
            if (result == null) {
                return "null";
            }
            return objectMapper.writeValueAsString(maskSensitiveFields(result, sensitiveFields));
        } catch (Exception e) {
            return "serialization_error: " + e.getMessage();
        }
    }

    private Object maskSensitiveFields(Object obj, String[] sensitiveFields) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            for (String field : sensitiveFields) {
                if (str.toLowerCase().contains(field.toLowerCase())) {
                    return "***MASKED***";
                }
            }
            return str;
        }
        if (obj instanceof Map) {
            Map<String, Object> map = new HashMap<>((Map<String, Object>) obj);
            for (String field : sensitiveFields) {
                if (map.containsKey(field)) {
                    map.put(field, "***MASKED***");
                }
            }
            return map;
        }
        return obj;
    }
}