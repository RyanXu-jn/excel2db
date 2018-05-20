package com.ryantsui;

import com.alibaba.fastjson.JSONObject;
import com.ryantsui.entity.JsonMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
public class Excel2dbApplication {
	public static void main(String[] args) {
		SpringApplication.run(Excel2dbApplication.class, args);
	}
}
