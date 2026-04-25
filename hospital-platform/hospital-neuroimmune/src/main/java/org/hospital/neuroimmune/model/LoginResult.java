package org.hospital.neuroimmune.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录结果封装类
 * 用于AuthService返回登录结果
 */
public class LoginResult {
    private boolean success;
    private String token;
    private Object user;
    private String role;
    private String errorMessage;

    private LoginResult() {}

    /**
     * 创建成功的登录结果
     */
    public static LoginResult ok(String token, Object user, String role) {
        LoginResult result = new LoginResult();
        result.success = true;
        result.token = token;
        result.user = user;
        result.role = role;
        return result;
    }

    /**
     * 创建失败的登录结果
     */
    public static LoginResult fail(String message) {
        LoginResult result = new LoginResult();
        result.success = false;
        result.errorMessage = message;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public Object getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 转换为API响应数据Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        if (success) {
            data.put("token", token);
            data.put("user", user);
            data.put("role", role);
        }
        return data;
    }
}