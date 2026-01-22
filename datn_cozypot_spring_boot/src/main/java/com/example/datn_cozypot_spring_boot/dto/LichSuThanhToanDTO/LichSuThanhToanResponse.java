package com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class LichSuThanhToanResponse {
    private String maGiaoDich;
    private String tenPhuongThuc;
    private BigDecimal soTienThanhToan;
    private Instant ngayThanhToan;
    private Integer trangThai;
    private Integer loaiGiaoDich; //1: Thanh toán, 2: Đặt cọc, 3: Hoàn tiền
    private String ghiChu;
}
