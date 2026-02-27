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
    private Integer soNguoi;
    private Integer trangThai;
    private String ghiChu;

    // Các thông tin bổ trợ để hiển thị UI mà không gây loop
    private Integer idBanAn;
    private String maBan;
    private String tenKhuVuc;
    private Integer tang;

    private List<ChiTietMonResponse> chiTiet;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietMonResponse {
        private Integer idChiTietHd; // THÊM TRƯỜNG NÀY ĐỂ LƯU KHÓA CHÍNH (SỐ 12, 13...)
        private Integer id; // Đây là originalId (ID của món lẻ hoặc set lẩu)
        private String tenMon;
        private Integer trangThaiMon;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;

        private String type; // "FOOD" hoặc "SET"
        private Integer idChiTietMonAn;
        private Integer idSetLau;

    }
}
