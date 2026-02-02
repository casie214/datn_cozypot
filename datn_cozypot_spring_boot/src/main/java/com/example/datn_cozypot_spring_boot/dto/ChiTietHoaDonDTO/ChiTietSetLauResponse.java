package com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSetLauResponse {
    private String tenMon;
    private String donVi;
    private Integer soLuong;
}
