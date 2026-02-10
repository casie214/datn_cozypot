package com.example.datn_cozypot_spring_boot.dto.setLauChiTiet;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetLauChiTietRequest {
    private Integer idChiTietMonAn; // ID của món ăn (thịt, rau...)
    private Integer soLuong;        // Số lượng
}
