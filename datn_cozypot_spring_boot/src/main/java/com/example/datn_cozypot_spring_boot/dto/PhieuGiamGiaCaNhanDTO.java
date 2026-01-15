package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PhieuGiamGiaCaNhanDTO {
    private Integer id;

    private String maGiamGiaCaNhan;

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Integer idKhachHang;

    @NotNull(message = "Vui lòng chọn phiếu giảm giá")
    private Integer idPhieuGiamGia;

    private Integer idHoaDonThanhToan;

    private Instant ngayNhan;

    private Instant ngaySuDung;

    private Integer trangThaiSuDung;

    private String nguonGoc;

    private String ghiChu;

    private Instant ngayHetHan;

    private String tenKhachHang;

    private String tenPhieuGiamGia;
}
