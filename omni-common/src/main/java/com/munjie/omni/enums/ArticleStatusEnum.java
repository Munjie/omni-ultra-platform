package com.munjie.omni.enums;
 
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),

    PUBLISH(1, "已发布"),



    ;

    @EnumValue
    private final int code;

    @JsonValue
    private final String text;

    ArticleStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }
}