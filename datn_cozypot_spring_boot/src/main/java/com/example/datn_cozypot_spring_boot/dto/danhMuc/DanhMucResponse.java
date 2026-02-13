package com.example.datn_cozypot_spring_boot.dto.danhMuc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhMucResponse {
    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private String moTa;

    // Audit fields
    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;
    private Integer soLuongMon;
}

