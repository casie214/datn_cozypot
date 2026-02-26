package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.Data;

@Data
public class CreateBanFullRequest {
    private Integer soNguoiToiDa;
    private Integer loaiDatBan;
    private Integer tang;
    private String tenKhuVuc;
}
