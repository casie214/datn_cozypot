package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NhanVienResponse {
    private Integer id;
    private String maNhanVien;
    private String hoTenNhanVien;
    private String sdtNhanVien;
    private String tenDangNhap;
    private String matKhauDangNhap;
    private String email;
    private LocalDate ngaySinh;
    private LocalDate ngayVaoLam;
    private String diaChi;
    private Boolean gioiTinh;
    private Integer trangThaiLamViec;
    private Integer idVaiTro;
    private String tenVaiTro;
}