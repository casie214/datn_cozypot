package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NhanVienRequest {
    private String hoTenNhanVien;
    private String sdtNhanVien;
    private String tenDangNhap;
    private String matKhauDangNhap;
    private Integer trangThaiLamViec;
    private LocalDate ngaySinh;
    private LocalDate ngayVaoLam;
    private String diaChi;
    private String email;
    private Boolean gioiTinh;
    private Integer idVaiTro;
}
