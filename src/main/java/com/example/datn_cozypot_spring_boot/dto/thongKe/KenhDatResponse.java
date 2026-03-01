package com.example.datn_cozypot_spring_boot.dto.thongKe;


public class KenhDatResponse {

    private String tenKenh;
    private Long soLuong;

    public KenhDatResponse(String tenKenh, Long soLuong) {
        this.tenKenh = tenKenh;
        this.soLuong = soLuong;
    }

    public String getTenKenh() {
        return tenKenh;
    }

    public Long getSoLuong() {
        return soLuong;
    }
}
