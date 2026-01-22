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
    private BigDecimal tongTienChuaGiam;
    private BigDecimal soTienDaGiam;
    private BigDecimal tongTienThanhToan;
    private BigDecimal tienCoc;
    private BigDecimal tienHoanTra;
    private Integer trangThaiHoanTien;     // 0: Ko cần hoàn, 1: Chờ hoàn, 2: Đã hoàn, 3: Không hoàn
    private Integer trangThaiHoaDon;       // 0: Hủy, 1: Đang phục vụ, 2: Hoàn thành, 3: Chờ nhận bàn
    private Instant thoiGianTao;
    private Integer hinhThucDat;
    private Integer soLuongKhach;
    private BigDecimal vatApDung;
}