package org.hospital.common.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    ADMIN("admin", "管理员"),
    DOCTOR("doctor", "医生"),
    PATIENT("patient", "患者");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        if (code == null) return null;
        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return null;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isDoctor() {
        return this == DOCTOR;
    }

    public boolean isPatient() {
        return this == PATIENT;
    }
}