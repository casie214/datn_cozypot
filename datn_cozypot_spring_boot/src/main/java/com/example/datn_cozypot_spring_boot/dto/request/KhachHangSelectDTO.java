package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Data
@Builder
public class KhachHangSelectDTO {
    private Integer idKhachHang;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private String diaChi;
}
