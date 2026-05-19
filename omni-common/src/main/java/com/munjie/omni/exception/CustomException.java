package com.munjie.omni.exception;


import com.munjie.omni.enums.ResultEnum;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException  {
    //异常错误编码

    private int code ;
    //异常信息
    private String message;

    private CustomException(){}

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public CustomException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public CustomException(int code, Throwable cause) {
        this(code, null, cause);
    }

    public CustomException(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
    }

    public CustomException(String message) {
        this.code = 500;
        this.message = message;
    }

    public CustomException(ResultEnum resultEnum, String message) {
        this.code = resultEnum.getCode();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
