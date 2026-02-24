package com.example.datn_cozypot_spring_boot.dto.DonVi;

import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongItemRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
import lombok.Data;

import java.util.List;

@Data
public class DonViRequest {
    private String tenDonVi;
    private String moTa;

    private List<DinhLuongItemRequest> values;

    private List<Integer> listIdDanhMuc;
}