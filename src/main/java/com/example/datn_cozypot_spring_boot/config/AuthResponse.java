package com.example.datn_cozypot_spring_boot.config;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String refreshToken;
    private String role;

    private Integer id;
    private String username;
    private String hoTen;

    public AuthResponse(String accessToken, String refreshToken, String role, Integer id, String username, String hoTen) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.id = id;
        this.username = username;
        this.hoTen = hoTen;
    }
}
