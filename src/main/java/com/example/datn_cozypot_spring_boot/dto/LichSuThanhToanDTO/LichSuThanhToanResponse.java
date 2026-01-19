package com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO;

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
}
