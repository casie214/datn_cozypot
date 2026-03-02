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
    private String email; // Bổ sung trường email
    private String sdt;

    // Cập nhật constructor để nhận thêm email (đứng trước sdt để khớp với thứ tự truyền ở Controller)
    public AuthResponse(String accessToken, String refreshToken, String role, Integer id, String username, String hoTen, String email, String sdt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.id = id;
        this.username = username;
        this.hoTen = hoTen;
        this.email = email;
        this.sdt = sdt;
    }
}