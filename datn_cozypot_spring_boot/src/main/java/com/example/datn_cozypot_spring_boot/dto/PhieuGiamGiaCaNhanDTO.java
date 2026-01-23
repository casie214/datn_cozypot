package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhieuGiamGiaCaNhanDTO {
    private Integer id;

    private String maPhieu;

    private String tenPhieu;

    private String tenKhachHang;

    private String emailKhachHang;

    private Integer trangThai;
}
