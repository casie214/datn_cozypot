package com.example.datn_cozypot_spring_boot.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreatePhieuDatBanFullRequest {
    private Integer idBanAn;

    // Nếu chọn khách cũ
    private Integer idKhachHang;

    // Nếu khách mới
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianDat;
    private Integer hinhThucDat;
    private Integer soLuongKhach;
}
