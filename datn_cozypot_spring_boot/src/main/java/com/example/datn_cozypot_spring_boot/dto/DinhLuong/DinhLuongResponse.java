package com.example.datn_cozypot_spring_boot.dto.DinhLuong;

import com.example.datn_cozypot_spring_boot.dto.DonVi.DinhLuongSimpleResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucShortResponse;
import lombok.Data;

import java.util.List;

@Data
public class DinhLuongResponse {
    private Integer id;
    private String tenDonVi;
    private String moTa;

    private List<DinhLuongSimpleResponse> giaTri;

    private List<DanhMucShortResponse> listDanhMuc;
}
