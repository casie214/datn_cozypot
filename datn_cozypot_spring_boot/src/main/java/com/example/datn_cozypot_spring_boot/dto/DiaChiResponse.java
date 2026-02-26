package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaChiResponse {
    private Integer id;
    private String hoTenNhan;
    private String soDienThoaiNhan;
    private String idTinhThanh;
    private String idQuanHuyen;
    private String idPhuongXa;
    private String diaChiChiTiet;
    private Boolean laMacDinh;
}