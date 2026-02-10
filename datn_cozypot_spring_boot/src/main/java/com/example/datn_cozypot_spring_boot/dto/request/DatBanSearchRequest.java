package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DatBanSearchRequest {
    private String soDienThoai;
    private Integer trangThai;
    private LocalDate thoiGianDat;
}
