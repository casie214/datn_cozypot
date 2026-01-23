package com.example.datn_cozypot_spring_boot.dto;

<<<<<<< HEAD
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhieuGiamGiaCaNhanDTO {
    private Integer id;

    private String maPhieu;

    private String tenPhieu;

    private String tenKhachHang;

    private String emailKhachHang;

    private Integer trangThai;
=======
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
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
}
