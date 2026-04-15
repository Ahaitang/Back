package org.hospital.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 统一定义系统错误码
 */
@Getter
public enum ErrorCode {

    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "参数错误"),
    PARAM_MISSING(400, "缺少必填参数"),
    PARAM_INVALID(400, "参数格式错误"),
    PARAM_RANGE_ERROR(400, "参数范围错误"),

    UNAUTHORIZED(401, "未认证或认证失败"),
    TOKEN_EXPIRED(401, "Token已过期"),
    TOKEN_INVALID(401, "Token无效"),
    LOGIN_FAILED(401, "用户名或密码错误"),

    FORBIDDEN(403, "权限不足"),
    ACCESS_DENIED(403, "拒绝访问"),

    NOT_FOUND(404, "资源不存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    PATIENT_NOT_FOUND(404, "患者不存在"),
    DOCTOR_NOT_FOUND(404, "医生不存在"),
    RECORD_NOT_FOUND(404, "记录不存在"),

    CONFLICT(409, "资源冲突"),
    DUPLICATE_KEY(409, "数据已存在"),
    DUPLICATE_PHONE(409, "手机号已存在"),
    DUPLICATE_USERNAME(409, "用户名已存在"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(500, "数据库操作失败"),
    REDIS_ERROR(500, "Redis操作失败"),
    FILE_UPLOAD_ERROR(500, "文件上传失败"),
    EXCEL_PARSE_ERROR(500, "Excel解析失败"),

    // ========== 业务错误 ==========
    IMPORT_ERROR(500, "导入失败"),
    IMPORT_PARTIAL_SUCCESS(500, "部分数据导入失败"),
    PASSWORD_MISMATCH(400, "密码不匹配"),
    PASSWORD_TOO_SIMPLE(400, "密码过于简单"),
    USER_OFFLINE(404, "用户已离线"),
    SESSION_EXPIRED(401, "会话已过期");

    /** 错误码 */
    private final int code;

    /** 错误消息 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}