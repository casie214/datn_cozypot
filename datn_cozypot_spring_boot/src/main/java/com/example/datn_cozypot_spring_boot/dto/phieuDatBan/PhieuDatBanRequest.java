package com.example.datn_cozypot_spring_boot.dto.phieuDatBan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhieuDatBanRequest {
    private Integer idKhachHang;
    private Integer idBanAn;
    private String tenKhachHangThoiVu;
    private String sdtThoiVu;
    private LocalDateTime thoiGianDat;
    private Integer soNguoi;
    private String ghiChu;
    private BigDecimal tongTien;
    private List<ChiTietMonAnRequest> chiTiet;

    @Data
    public static class ChiTietMonAnRequest {
        private Integer idChiTietMonAn;
        private Integer idSetLau;
        private Integer soLuong;
        private BigDecimal donGia;
    }
}
