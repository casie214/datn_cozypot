package com.example.datn_cozypot_spring_boot.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class TestController {
    @GetMapping("/status")
    public Map<String, String> checkStatus() {
        return Map.of(
                "status", "active",
                "message", "Backend is running successfully!",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }
}
