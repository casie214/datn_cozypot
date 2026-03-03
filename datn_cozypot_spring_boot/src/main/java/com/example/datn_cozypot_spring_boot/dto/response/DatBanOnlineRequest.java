package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatBanOnlineRequest {
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime thoiGianDat;
    private Integer soNguoi;
    private String ghiChu;

    private Integer idBanAn;

    private BigDecimal tongTien;
    private BigDecimal tienCoc;
    private List<PhieuDatBanRequest.ChiTietMonAnRequest> chiTiet;
}
