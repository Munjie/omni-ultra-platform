package com.munjie.omni.enums;
 
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
 
@Getter
public enum TypeEnum {

    SCORE(1, "成绩分析"),

    DISTANCE(2, "路程计算");
 
    @EnumValue
    private final int code;
 
    @JsonValue
    private final String text;

    TypeEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }
}