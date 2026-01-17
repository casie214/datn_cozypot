package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private LocalDate ngaySinh;
    private Integer diemTichLuy;
    private Instant ngayTaoTaiKhoan;
    private Boolean gioiTinh;
    private String tenDangNhap;
    private String matKhauDangNhap;
    private Integer trangThai;
    private String diaChi;

}