package org.hospital.neuroimmune.model;

/**
 * 注册结果封装类
 * 用于AuthService返回注册结果
 */
public class RegisterResult {
    private boolean success;
    private Long patientId;
    private String status;
    private String errorMessage;

    private RegisterResult() {}

    /**
     * 创建成功的注册结果
     */
    public static RegisterResult ok(Long patientId, String status) {
        RegisterResult result = new RegisterResult();
        result.success = true;
        result.patientId = patientId;
        result.status = status;
        return result;
    }

    /**
     * 创建失败的注册结果
     */
    public static RegisterResult fail(String errorMessage) {
        RegisterResult result = new RegisterResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}