package com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class HoaDonThanhToanResponse {

    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private List<String> danhSachTenBan; // Trường này sẽ được gán thủ công ở Service
    private BigDecimal tongTienChuaGiam;
    private BigDecimal soTienDaGiam;
    private BigDecimal tongTienThanhToan;
    private BigDecimal tienCoc;
    private BigDecimal tienHoanTra;
    private Integer trangThaiHoaDon;
    private Instant thoiGianTao;
    private Integer hinhThucDat;
    private LocalDateTime thoiGianDat;
    private Integer soLuongKhach;
    private BigDecimal vatApDung;

    // 🚨 CONSTRUCTOR NÀY DÀNH RIÊNG CHO REPOSITORY (15 Tham số - KHÔNG CÓ List danhSachTenBan)
    public HoaDonThanhToanResponse(
            Integer id, String maHoaDon, String tenKhachHang, String sdtKhachHang,
            BigDecimal tongTienChuaGiam, BigDecimal soTienDaGiam, BigDecimal tongTienThanhToan,
            BigDecimal tienCoc, BigDecimal tienHoanTra, Integer trangThaiHoaDon,
            Instant thoiGianTao, Integer hinhThucDat, LocalDateTime thoiGianDat,
            Integer soLuongKhach, BigDecimal vatApDung
    ) {
        this.id = id;
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhachHang = sdtKhachHang;
        this.tongTienChuaGiam = tongTienChuaGiam;
        this.soTienDaGiam = soTienDaGiam;
        this.tongTienThanhToan = tongTienThanhToan;
        this.tienCoc = tienCoc;
        this.tienHoanTra = tienHoanTra;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.thoiGianTao = thoiGianTao;
        this.hinhThucDat = hinhThucDat;
        this.thoiGianDat = thoiGianDat;
        this.soLuongKhach = soLuongKhach;
        this.vatApDung = vatApDung;
    }

    // 🚨 CONSTRUCTOR ĐẦY ĐỦ (16 Tham số - CÓ List danhSachTenBan) - Dùng khi cần copy data
    public HoaDonThanhToanResponse(
            Integer id, String maHoaDon, String tenKhachHang, String sdtKhachHang,
            List<String> danhSachTenBan, BigDecimal tongTienChuaGiam, BigDecimal soTienDaGiam,
            BigDecimal tongTienThanhToan, BigDecimal tienCoc, BigDecimal tienHoanTra,
            Integer trangThaiHoaDon, Instant thoiGianTao, Integer hinhThucDat,
            LocalDateTime thoiGianDat, Integer soLuongKhach, BigDecimal vatApDung
    ) {
        this(id, maHoaDon, tenKhachHang, sdtKhachHang, tongTienChuaGiam, soTienDaGiam,
                tongTienThanhToan, tienCoc, tienHoanTra, trangThaiHoaDon, thoiGianTao,
                hinhThucDat, thoiGianDat, soLuongKhach, vatApDung);
        this.danhSachTenBan = danhSachTenBan;
    }
}