package com.example.datn_cozypot_spring_boot.dto.MonAn;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonAnInSetResponse {
    private Integer idMon;
    private String tenMon;       // VD: Ba chỉ bò
    private String dinhLuong;    // VD: 100g
    private Integer soLuong;     // VD: 2
    private String hinhAnhMon;
    private BigDecimal giaBan;
}
