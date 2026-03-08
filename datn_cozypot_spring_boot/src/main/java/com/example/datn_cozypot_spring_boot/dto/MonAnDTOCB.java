package com.example.datn_cozypot_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonAnDTOCB {
    private String tenMon;
    private BigDecimal gia;
    private String thuocDanhMuc;
    private String hinhAnh;
}
