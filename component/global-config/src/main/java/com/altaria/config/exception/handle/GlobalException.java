package com.altaria.config.exception.handle;

import com.altaria.common.pojos.common.Result;
import com.altaria.config.exception.BaseException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleValidationExceptions(ConstraintViolationException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public Result handleBaseExceptions(BaseException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result paramEx(BindException be){
        Result result = new Result();
        result.setCode(500);
        result.setMsg(be.getBindingResult().getFieldError().getDefaultMessage());
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result err(Exception e){
        return Result.error();
    }
}
