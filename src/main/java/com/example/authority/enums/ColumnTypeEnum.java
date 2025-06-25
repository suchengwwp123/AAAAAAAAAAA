package com.example.authority.enums;

import lombok.Getter;

/**
 * 数据库字段类型枚举
 */
@Getter
public enum ColumnTypeEnum {

    VARCHAR_64("varchar(64)"),
    VARCHAR_255("varchar(255)"),
    INT_1("int(1)"),
    INT_5("int(5)"),
    INT_10("int(10)"),
    BIGINT_20("bigint(20)"),
    DOUBLE_10_2("double(10,2)"),
    DATETIME("datetime"),
    TEXT("text"),
    LONGTEXT("longtext");

    private final String value;

    ColumnTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据值获取对应的枚举
     *
     * @param value 字段类型值
     * @return 对应的枚举
     */
    public static ColumnTypeEnum fromValue(String value) {
        for (ColumnTypeEnum type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的字段类型: " + value);
    }
}
