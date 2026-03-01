package com.example.datn_cozypot_spring_boot.dto.request;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DatBanUpdateRequest {
        private Integer trangThaiHoaDon;

        private Integer id;

        private Integer idNhanVien;

        private Integer idKhachHang;

        private Integer idBanAnMoi;

        @NotNull(message = "ID Bàn không được trống")
        private Integer idBanAn;

        private String maDatBanGoc;

        private Integer trangThai;

        private BigDecimal tienMat;

        private Integer trangThaiPhieu;

        private Double vatApDung;
}
