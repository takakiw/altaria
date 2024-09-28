package com.altaria.common.exception.handle;

import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
public class GlobalException {


    @ExceptionHandler(Exception.class)
    public Result err(Exception e){
        return Result.error(e.getMessage());
    }
}
