package com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO;

import lombok.*;

import java.time.Instant;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LichSuHoaDonRequest {
    private Integer idHoaDon;
    private Integer idNhanVien;
    private String hanhDong;
    private Instant thoiGianThucHien;
    private String lyDoThucHien;
    private Integer trangThaiLichSuTruocDo;
    private Integer trangThaiLichSuMoi;
    private Integer trangThaiHoaDonMoi;
}
