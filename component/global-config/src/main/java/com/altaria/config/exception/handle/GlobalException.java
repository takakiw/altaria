package com.altaria.config.exception.handle;

import com.altaria.common.pojos.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
public class GlobalException {


    @ExceptionHandler(Exception.class)
    public Result err(Exception e){
        if (e.getMessage() == null){
            return Result.error();
        }
        return Result.error(e.getMessage());
    }
}
