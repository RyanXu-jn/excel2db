package com.ryantsui.controller;

import com.ryantsui.entity.JsonMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常控制类.
 * Created by xufy on 2018/5/19.
 */
@ControllerAdvice
public class ExceptionHander {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonMessage handleException(Exception e) {
        return new JsonMessage().failure(e.getMessage());
    }
}
