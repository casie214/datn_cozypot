package com.example.datn_cozypot_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaResponseDTO {
    private Integer id;
    private String maPhieuGiamGia;
    private String codeGiamGia;
    private String tenPhieuGiamGia;
    private Integer loaiGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriGiamToiDa;
    private BigDecimal donHangToiThieu;
    private Integer doiTuong;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer soLuong;
    private Integer trangThai;
    // Để Backend lấy ra gửi mail
    private List<KhachHangResponse> danhSachKhachHang;

    public List<KhachHangResponse> getDanhSachKhachHang() {
        return danhSachKhachHang;
    }

    public void setDanhSachKhachHang(List<KhachHangResponse> danhSachKhachHang) {
        this.danhSachKhachHang = danhSachKhachHang;
    }
    private Integer idDotKhuyenMai;
    private String tenDotKhuyenMai;






}
