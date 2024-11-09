package com.altaria.config.exception;

import com.altaria.common.enums.StatusCodeEnum;

public class BaseException extends RuntimeException{
    public BaseException(String message) {
        super(message);
    }
    public BaseException(){
        super();
    }

    public BaseException(StatusCodeEnum statusCodeEnum){
        super(statusCodeEnum.getMessage());
    }
}

