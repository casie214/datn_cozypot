package com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GopBanRequest {
    private Integer idHoaDonChu;    // Hóa đơn B (Người nuốt)
    private Integer idHoaDonBiNuot; // Hóa đơn A (Kẻ bị nuốt)
    private Integer idNhanVien;     // Nhân viên thực hiện thao tác
}