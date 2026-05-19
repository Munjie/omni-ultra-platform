package com.munjie.omni.enums;
 
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    FAIL(0, "运行失败"),

    RUNNING(1, "运行中"),

    SUCCESS(1, "运行成功"),

    ;

    @EnumValue
    private final int code;

    @JsonValue
    private final String text;

    TaskStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }
}