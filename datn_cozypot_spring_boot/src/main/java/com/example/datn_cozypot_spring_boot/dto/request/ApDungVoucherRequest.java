package com.example.datn_cozypot_spring_boot.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ApDungVoucherRequest {
    private Integer idHoaDon;
    private Integer idKhachHang; // Có thể null nếu khách vãng lai
    private Integer idPhieuGiamGia; // Dùng khi click chọn từ danh sách
    private String codeGiamGia; // Dùng khi khách nhập tay
    private BigDecimal tongTienHienTai; // Tổng tiền món hiện tại của bàn để check điều kiện
}