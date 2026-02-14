package com.example.datn_cozypot_spring_boot.dto.DonVi;

import lombok.Data;

import java.util.List;

@Data
public class DonViResponse {
    private Integer id;
    private String tenDonVi;
    private String moTa;
    private List<DinhLuongSimpleResponse> values;
    private List<Integer> listIdDanhMuc;
}
