package com.example.datn_cozypot_spring_boot.config;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
<<<<<<< HEAD
    private String role; // "ADMIN", "EMPLOYEE", hoặc "USER"

    public AuthResponse(String accessToken, String role) {
        this.accessToken = accessToken;
=======
    private String refreshToken;
    private String role;

    public AuthResponse(String accessToken, String refreshToken, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
        this.role = role;
    }
}
