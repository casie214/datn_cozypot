package com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class ChiTietHoaDonResponse {
    private Integer id;
    private String tenMon;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String ghiChu;
    private String trangThaiText;
    private Integer trangThaiCode;
    private Integer idSetLau;
}
