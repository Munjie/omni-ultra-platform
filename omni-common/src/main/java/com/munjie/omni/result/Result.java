package com.munjie.omni.result;


import com.munjie.omni.enums.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public Result(Object body) {
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
    }

    public static Result ok() {
        return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), "");
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultEnum.SUCCESS.getCode(), message, data);
    }

    public static Result<?> error() {
        return new Result<>(ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getMessage(), null);
    }

    public static Result<?> error(String message) {
        return new Result<>(ResultEnum.ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>(code, message, null);
    }

    public static <T> Result<T> error(ResultEnum resultStatus) {
        return new Result<T>(resultStatus.getCode(), resultStatus.getMessage(), null);
    }


    public static Result<?> error(IResult errorResult) {
        return new Result<>(errorResult.getCode(), errorResult.getMessage(), null);
    }

    public static <T> Result<T> instance(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}