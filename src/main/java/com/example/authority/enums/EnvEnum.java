package com.example.authority.enums;

/**
 * 系统环境枚举
 */
public enum EnvEnum {
    DEV("dev", "开发环境"),
    TEST("test", "测试环境"),

    PROD("prod", "生产环境"),
    DEMO("demo", "演示环境");  // ✅ 新增演示环境

    private final String code;
    private final String desc;

    EnvEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static EnvEnum fromCode(String code) {
        for (EnvEnum env : values()) {
            if (env.code.equalsIgnoreCase(code)) {
                return env;
            }
        }
        throw new IllegalArgumentException("未知环境标识: " + code);
    }

    /**
     * 判断是否为生产环境
     */
    public static boolean isProd(String code) {
        return PROD.code.equalsIgnoreCase(code);
    }

    /**
     * 判断是否为开发环境
     */
    public static boolean isDev(String code) {
        return DEV.code.equalsIgnoreCase(code);
    }

    /**
     * 判断是否为演示环境
     */
    public static boolean isDemo(String code) {
        return DEMO.code.equalsIgnoreCase(code);
    }
    /**
     * 判断是否为测试环境
     */
    public static boolean isTest(String code) {
        return TEST.code.equalsIgnoreCase(code);
    }
}
