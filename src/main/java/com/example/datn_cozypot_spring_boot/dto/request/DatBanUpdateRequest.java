package com.example.datn_cozypot_spring_boot.dto.request;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DatBanUpdateRequest {
        private Integer id;

        @NotNull(message = "ID Bàn không được trống")
        private Integer idBanAn;

        private Integer trangThai;
}
