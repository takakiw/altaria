package com.altaria.config.exception;

import com.altaria.common.enums.StatusCodeEnum;

public class BaseException extends Exception {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(StatusCodeEnum statusCodeEnum) {
        super(statusCodeEnum.getMessage());
    }
}
