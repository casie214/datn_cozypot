package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class KhachHangThongKeResponse {

    private Long id;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private LocalDate ngaySinh;

    // thống kê
    private Integer soLanDatTrongThang;
    private Long tongChiTieuTrongThang;

    // QUAN TRỌNG
    private LocalDateTime lanDatGanNhat;
}
