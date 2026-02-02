package com.example.datn_cozypot_spring_boot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTrangThaiPhieuRequest {
    @NotNull(message = "ID phiếu đặt không được trống")
    private Integer id;

    @NotNull(message = "Trạng thái không được trống")
    private Integer trangThai;
}
