package com.example.datn_cozypot_spring_boot.dto;

import lombok.Data;

import java.util.List;

@Data
public class XepBanRequest {
    private Integer idPhieuDatBan;
    private List<Integer> danhSachIdBanAn;
}
