package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaChiResponse {
    private Integer id;
    private String hoTenNhan;
    private String soDienThoaiNhan;
    private String thongTinDiaChi;
    private Boolean laMacDinh;
}