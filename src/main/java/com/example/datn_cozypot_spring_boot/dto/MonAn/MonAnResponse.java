package com.example.datn_cozypot_spring_boot.dto.MonAn;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonAnResponse {
    private Integer id;
    private String maMon;
    private String tenMon;
    private BigDecimal giaBan;
    private String hinhAnh;
    private String moTa;

    private String tenDanhMuc;
    private Integer idDanhMuc;

    private String tenDinhLuong;
    private String giaTriDinhLuong;
    private String kichCo;
    private Integer idDinhLuong;
    private Integer trangThai;

    public String getTenHienThi() {
        return tenMon + " (" + (giaTriDinhLuong != null ? giaTriDinhLuong : tenDinhLuong) + ")";
    }
}