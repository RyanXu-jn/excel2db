package com.ryantsui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Excel2dbApplication {
	public static void main(String[] args) {
		SpringApplication.run(Excel2dbApplication.class, args);
	}
}
