package com.example.datn_cozypot_spring_boot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateBanAnRequest {
    private Integer id;
    private String maBan;
    @NotNull(message = "Số chỗ không được trống")
    private Integer soNguoiToiDa;
    @NotNull(message = "Loại đặt bàn không được trống")
    private Integer loaiDatBan;
    @NotNull(message = "Khu vực không được trống")
    private Integer idKhuVuc;
    @NotNull(message = "Trạng thái không được trống")
    private Integer trangThai;
    private Instant ngayTao;
    private String nguoiTao;
}
