package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailHuyDatBanDTO {
    private String email;
    private String tenKhachHang;
    private String maPhieuDatBan;

    private String nguoiHuy;
    private String lyDoHuy;
    private String tienHoanTra;
}