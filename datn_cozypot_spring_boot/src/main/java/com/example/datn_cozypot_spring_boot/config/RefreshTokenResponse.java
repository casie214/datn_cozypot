package com.example.datn_cozypot_spring_boot.config;

import lombok.Data;

@Data
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String role;

    public RefreshTokenResponse(String accessToken, String refreshToken, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public RefreshTokenResponse() {
    }
}
