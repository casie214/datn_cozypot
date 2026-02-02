package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VaiTroResponse {
    private Integer id;
    private String maVaiTro;
    private String tenVaiTro;
    private String moTaVaiTro;
    private Integer trangThai;
}