package com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DanhMucChiTietResponse {
    private Integer id;

    private Integer idDanhMuc;
    private String tenDanhMuc;

    private String maDanhMucChiTiet;
    private Integer idDanhMucChiTiet;
    private String tenDanhMucChiTiet;
    private String moTa;

    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;
}
