package com.example.datn_cozypot_spring_boot.dto.phieuDatBan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuDatBanResponse {
    private Integer id;
    private String maPhieu; // Giúp hiện "Mã phiếu: PDB001"
    private String tenKhachHang;
    private String sdtKhachHang;
    private LocalDateTime thoiGianDat;
    private Integer soNguoiBanNay;
    private Integer soNguoi;
    private Integer trangThai;
    private String ghiChu;

    // 1. THÔNG TIN BÀN HIỆN TẠI (Bàn mà nhân viên đang click vào sơ đồ)
    private Integer idBanAn;
    private String maBan;
    private String tenKhuVuc;
    private Integer tang;

    // 2. THÔNG TIN QUẢN LÝ GỘP BÀN (Kiến trúc N-N)
    // Danh sách tất cả các bàn đang thuộc phiếu này
    private List<BanAnInfo> danhSachBan;

    // Các cờ (flag) giúp Frontend biết đường khóa nút Thanh toán
    private Boolean isBanPhu;
    private String tenBanChinh;
    private Integer idBanChinh;

    // 3. THÔNG TIN HÓA ĐƠN & TÀI CHÍNH
    private Integer idHoaDon;
    private Integer idKhachHang;
    private BigDecimal tongTienChuaGiam;
    private BigDecimal soTienDaGiam;
    private BigDecimal tienCoc;
    private BigDecimal tongTienThanhToan;
    private BigDecimal vatApDung;

    // 4. DANH SÁCH MÓN ĂN
    private List<ChiTietMonResponse> chiTiet;

    // ==========================================
    // CÁC CLASS CON BỔ TRỢ
    // ==========================================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BanAnInfo {
        private Integer id;
        private String maBan;
        private String tenKhuVuc;
        private Integer tang;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietMonResponse {
        private Integer idChiTietHd; // Khóa chính của chi tiết hóa đơn (SỐ 12, 13...)
        private Integer id; // Đây là originalId (ID của món lẻ hoặc set lẩu)
        private String tenMon;
        private Integer trangThaiMon;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;

        private String type; // "FOOD" hoặc "SET"
        private Integer idChiTietMonAn;
        private Integer idSetLau;
        private Integer apDungLoaiVat;

        // Dùng để Frontend nhóm món ăn hiển thị (Ví dụ: [Bàn S02] Ít cay)
        private String ghiChu;
        private String tenBanPhucVu;
    }
}