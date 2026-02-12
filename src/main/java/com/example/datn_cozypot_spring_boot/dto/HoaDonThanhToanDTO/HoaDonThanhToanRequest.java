package com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonThanhToanRequest {
    private Integer idBanAn;
    private Integer idNhanVien;

    private Integer idKhachHang;
    private Integer idPhieuDatBan;

    private List<ChiTietHoaDonRequest> chiTietHoaDon;
}
