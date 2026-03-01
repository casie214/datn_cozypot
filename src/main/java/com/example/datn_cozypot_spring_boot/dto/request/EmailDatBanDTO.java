package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDatBanDTO {
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String thoiGianDat;  // VD: "28/02/2026 19:30"
    private String tenBan;
    private String khuVuc;
    private int soLuongKhach;
    private String maPhieuDatBan; // mã phiếu để khách tra cứu
}
