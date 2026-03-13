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
    private Integer idPhieuGiamGia;
    private String maPhieuGiamGia;

    // 🚨 CONSTRUCTOR NÀY DÀNH RIÊNG CHO REPOSITORY (15 Tham số - KHÔNG CÓ List danhSachTenBan)
    public HoaDonThanhToanResponse(
            Integer id, String maHoaDon, String tenKhachHang, String sdtKhachHang,
            BigDecimal tongTienChuaGiam, BigDecimal soTienDaGiam, BigDecimal tongTienThanhToan,
            BigDecimal tienCoc, BigDecimal tienHoanTra, Integer trangThaiHoaDon,
            Instant thoiGianTao, Integer hinhThucDat, LocalDateTime thoiGianDat,
            Integer soLuongKhach, BigDecimal vatApDung,
            Integer idPhieuGiamGia, String maPhieuGiamGia // 🔥 THÊM 2 BIẾN NÀY VÀO CUỐI
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

        // 🔥 Gán giá trị
        this.idPhieuGiamGia = idPhieuGiamGia;
        this.maPhieuGiamGia = maPhieuGiamGia;
    }
}