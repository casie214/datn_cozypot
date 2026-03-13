package com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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
    private Integer trangThaiHoaDon;

    // thoiGianTao giữ nguyên Instant (vì Entity HoaDonThanhToan dùng Instant)
    private Instant thoiGianTao;

    private Integer hinhThucDat;

    // 🔥 SỬA Ở ĐÂY: Đổi từ Instant -> LocalDateTime
    private LocalDateTime thoiGianDat;

    private Integer soLuongKhach;
    private BigDecimal vatApDung;

    public HoaDonThanhToanResponse(
            Integer id,
            String maHoaDon,
            String tenKhachHang,
            String sdtKhachHang,
            String tenBan,
            BigDecimal tongTienChuaGiam,
            BigDecimal soTienDaGiam,
            BigDecimal tongTienThanhToan,
            BigDecimal tienCoc,
            BigDecimal tienHoanTra,
            Integer trangThaiHoaDon,
            Instant thoiGianTao,
            Integer hinhThucDat,
            // 🔥 SỬA Ở ĐÂY: Tham số constructor cũng phải là LocalDateTime
            LocalDateTime thoiGianDat,
            Integer soLuongKhach,
            BigDecimal vatApDung
    ) {
        this.id = id;
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhachHang = sdtKhachHang;
        this.tenBan = tenBan;
        this.tongTienChuaGiam = tongTienChuaGiam;
        this.soTienDaGiam = soTienDaGiam;
        this.tongTienThanhToan = tongTienThanhToan;
        this.tienCoc = tienCoc;
        this.tienHoanTra = tienHoanTra;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.thoiGianTao = thoiGianTao;
        this.hinhThucDat = hinhThucDat;
        this.thoiGianDat = thoiGianDat; // Gán đúng kiểu
        this.soLuongKhach = soLuongKhach;
        this.vatApDung = vatApDung;
    }
}