package com.github.abeatrizsc.financyx.enums;

public enum CategoryTypeEnum {
    INCOME("income"),
    EXPENSE("expense");

    private String value;

    CategoryTypeEnum(String value) {
        this.value = value;
    }

    public String getCategoryValue() {
        return value;
    }
}
