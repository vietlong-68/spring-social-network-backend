package com.spring.social_network.model;

public enum RoleType {
    ADMIN("ADMIN", "Administrator"),
    USER("USER", "Regular User");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromCode(String code) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getCode().equalsIgnoreCase(code)) {
                return roleType;
            }
        }
        throw new IllegalArgumentException("Mã loại vai trò không hợp lệ: " + code);
    }
}
