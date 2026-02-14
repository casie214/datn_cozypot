package com.example.datn_cozypot_spring_boot.dto.DinhLuong;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DinhLuongRequest {
    private Integer idDanhMuc;
    private String kichCo;
    private String dinhLuong;
    private String tenHienThi;
}