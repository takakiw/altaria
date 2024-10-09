package com.altaria.common.pojos.common;

import com.altaria.common.enums.StatusCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = StatusCodeEnum.SUCCESS.getCode();
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = StatusCodeEnum.SUCCESS.getCode();
        return result;
    }
    public static <T> Result<T> success(Integer code) {
        Result<T> result = new Result<T>();
        result.code = code;
        return result;
    }

    public static <T> Result<T> error(StatusCodeEnum statusCodeEnum) {
        Result result = new Result();
        result.msg = statusCodeEnum.getMessage();
        result.code = statusCodeEnum.getCode();
        return result;
    }

    public static <T> Result<T> error() {
        Result result = new Result();
        result.msg = StatusCodeEnum.ERROR.getMessage();
        result.code = StatusCodeEnum.ERROR.getCode();
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = StatusCodeEnum.ERROR.getCode();
        return result;
    }
}
