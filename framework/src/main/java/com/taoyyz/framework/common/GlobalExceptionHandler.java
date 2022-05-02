package com.taoyyz.framework.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.taoyyz.framework.common.exception.UserInfoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 15:31
 */
@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) {
        log.error("异常：" + e.getMessage());
        return Result.error();
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleException(RuntimeException e) {
        log.error("运行时异常：" + e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(UserInfoException.class)
    public Object handleException(UserInfoException e) {
        log.error("获取用户信息出错：" + e.getApiCode().getMessage());
        return Result.error(e.getApiCode());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public Object handleException(JsonProcessingException e) {
        log.error("(反)序列化异常：" + e.getMessage());
        return Result.error("序列化异常");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        log.warn(e.getParameter().getMethod().getName() + "参数异常：" + fieldError.getDefaultMessage());
        return Result.error(fieldError.getDefaultMessage());
    }
}
