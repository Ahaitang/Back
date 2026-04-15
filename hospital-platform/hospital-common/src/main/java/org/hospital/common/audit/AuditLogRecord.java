package org.hospital.common.audit;

/**
 * 审计日志记录结构
 */
public class AuditLogRecord {

    private final String timestamp;
    private final Long userId;
    private final String username;
    private final String role;
    private final String operation;
    private final String module;
    private final String description;
    private final String requestUri;
    private final String method;
    private final String ipAddress;
    private final String params;
    private final String result;
    private final String status;
    private final String errorMessage;
    private final long duration;

    public AuditLogRecord(String timestamp, Long userId, String username, String role,
                          String operation, String module, String description,
                          String requestUri, String method, String ipAddress,
                          String params, String result, String status,
                          String errorMessage, long duration) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.operation = operation;
        this.module = module;
        this.description = description;
        this.requestUri = requestUri;
        this.method = method;
        this.ipAddress = ipAddress;
        this.params = params;
        this.result = result;
        this.status = status;
        this.errorMessage = errorMessage;
        this.duration = duration;
    }

    /**
     * 输出为日志字符串（管道分隔，便于解析）
     */
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append("|");
        sb.append(userId != null ? userId : "null").append("|");
        sb.append(username).append("|");
        sb.append(role).append("|");
        sb.append(operation).append("|");
        sb.append(module).append("|");
        sb.append(description).append("|");
        sb.append(requestUri).append("|");
        sb.append(method).append("|");
        sb.append(ipAddress).append("|");
        sb.append(params).append("|");
        sb.append(result).append("|");
        sb.append(status).append("|");
        sb.append(errorMessage).append("|");
        sb.append(duration);
        return sb.toString();
    }

    // Getters
    public String getTimestamp() { return timestamp; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getOperation() { return operation; }
    public String getModule() { return module; }
    public String getDescription() { return description; }
    public String getRequestUri() { return requestUri; }
    public String getMethod() { return method; }
    public String getIpAddress() { return ipAddress; }
    public String getParams() { return params; }
    public String getResult() { return result; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public long getDuration() { return duration; }
}