package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class DatBanSearchRequest {
    private String soDienThoai;
    private Integer trangThai;
    private Instant thoiGianDat;
}
