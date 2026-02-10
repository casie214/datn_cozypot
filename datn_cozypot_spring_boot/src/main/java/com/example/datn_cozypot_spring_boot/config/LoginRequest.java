package com.example.datn_cozypot_spring_boot.config;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;

    private String password;
}
