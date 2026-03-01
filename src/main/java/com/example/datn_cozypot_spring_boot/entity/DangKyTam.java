package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "dang_ky_tam")
public class DangKyTam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(name = "data_json", columnDefinition = "NVARCHAR(MAX)", nullable = false) // Đổi thành "data_json"
    private String dataJson;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;
}