package com.example.datn_cozypot_spring_boot.dto.DinhLuong;

import lombok.Data;

@Data
public class DinhLuongItemRequest {
    private Integer id;       // Nếu có ID -> Update, Null -> Insert
    private String giaTri;
    private Integer trangThai;
}
