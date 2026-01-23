package com.example.datn_cozypot_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.scheduling.annotation.EnableAsync; // Thêm dòng này
=======
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
@SpringBootApplication
@EnableAsync // Thêm annotation này để cho phép chạy tác vụ ngầm
public class DatnCozypotSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatnCozypotSpringBootApplication.class, args);
    }

}