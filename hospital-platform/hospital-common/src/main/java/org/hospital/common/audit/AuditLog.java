package org.hospital.common.audit;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 用于标记需要记录审计日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作类型
     */
    OperationType operation();

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作描述
     */
    String description();

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResult() default false;

    /**
     * 敏感字段（不记录）
     */
    String[] sensitiveFields() default {"password", "token", "secret"};
}