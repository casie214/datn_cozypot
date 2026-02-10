package com.example.datn_cozypot_spring_boot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddBanAnRequest {
    @NotNull(message = "Số chỗ không được trống")
    private Integer soNguoiToiDa;
    @NotNull(message = "Loại đặt bàn không được trống")
    private Integer loaiDatBan;
    @NotNull(message = "Khu vực không được trống")
    private Integer idKhuVuc;
}
