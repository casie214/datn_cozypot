package com.example.datn_cozypot_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DatnCozypotSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnCozypotSpringBootApplication.class, args);
	}

}
