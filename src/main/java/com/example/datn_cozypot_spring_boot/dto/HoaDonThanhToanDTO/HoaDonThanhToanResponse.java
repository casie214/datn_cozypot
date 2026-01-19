package com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class HoaDonThanhToanResponse {

    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String tenBan;
    private BigDecimal tongTienThanhToan;
    private BigDecimal soTienDaGiam;
    private Integer trangThaiHoaDon;
    private Instant thoiGianTao;
    private Integer hinhThucDat;
}