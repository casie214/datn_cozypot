package com.example.datn_cozypot_spring_boot.dto.danhMuc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DanhMucShortResponse {
    private Integer id;
    private String tenDanhMuc;
}
