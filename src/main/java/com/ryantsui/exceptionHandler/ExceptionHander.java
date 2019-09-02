package com.ryantsui.exceptionHandler;

import com.ryantsui.entity.JsonMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 异常控制类.
 * Created by xufy on 2018/5/19.
 */
@ControllerAdvice
public class ExceptionHander {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JsonMessage handleException(Exception e) {
        return new JsonMessage().failure(e.getMessage());
    }
}
