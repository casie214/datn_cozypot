package com.example.datn_cozypot_spring_boot.dto.loaiLau;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiLauResponse {
    private Integer id;
    private String maLoaiSet;
    private String tenLoaiSet;
    private String moTa;

    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;
}