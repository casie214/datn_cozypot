package com.example.datn_cozypot_spring_boot.dto.DonVi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DinhLuongSimpleResponse {
    private Integer id;
    private String giaTri;
    private String tenHienThi;
    private Integer trangThai;
}