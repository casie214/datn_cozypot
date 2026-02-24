package com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSetLauResponse {
    private String tenMon;
    private String donVi;
    private Integer soLuong;

    private BigDecimal giaBan;
    private String hinhAnh;
    private String dinhLuong;

    public ChiTietSetLauResponse(String tenMon, String tenHienThi, Integer soLuong) {
        this.tenMon = tenMon;
        this.dinhLuong = tenHienThi;
        this.soLuong = soLuong;
    }
}
