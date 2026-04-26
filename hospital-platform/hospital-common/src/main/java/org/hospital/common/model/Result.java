package org.hospital.common.model;

import lombok.Data;

/**
 * 统一响应结果
 * 支持两种格式：
 * - QMG 格式: code=1 成功, code=0 失败
 * - Neuroimmune 格式: code=200 成功, code=500 失败
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // ========== Neuroimmune 格式 (code=200/500) ==========

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // ========== QMG 格式 (code=1/0) ==========

    /**
     * QMG 格式成功响应 (code=1)
     */
    public static <T> Result<T> qmgSuccess() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage("success");
        return result;
    }

    /**
     * QMG 格式成功响应带数据 (code=1)
     */
    public static <T> Result<T> qmgSuccess(T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * QMG 格式失败响应 (code=0)
     */
    public static <T> Result<T> qmgError(String message) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        return result;
    }

    /**
     * QMG 格式失败响应带code (code=自定义)
     */
    public static <T> Result<T> qmgError(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}