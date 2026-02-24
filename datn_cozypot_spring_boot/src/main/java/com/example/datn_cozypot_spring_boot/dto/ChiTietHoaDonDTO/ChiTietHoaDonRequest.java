package com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChiTietHoaDonRequest {
    private Integer idChiTietMonAn;
    private Integer idSetLau;
    private Integer soLuong;
    private BigDecimal donGia;
    private String ghiChu;
}
