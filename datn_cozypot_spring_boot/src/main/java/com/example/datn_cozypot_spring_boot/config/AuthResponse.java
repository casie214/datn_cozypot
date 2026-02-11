package com.example.datn_cozypot_spring_boot.config;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String role; // "ADMIN", "EMPLOYEE", hoặc "USER"

    public AuthResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.role = role;
    }
}
