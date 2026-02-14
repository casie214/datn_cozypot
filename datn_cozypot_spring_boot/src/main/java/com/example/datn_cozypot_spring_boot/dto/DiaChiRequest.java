package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaChiRequest {

    private Integer id;
    private String hoTenNhan;
    private String soDienThoaiNhan;
    @NotBlank(message = "Địa chỉ không được để trống")
    private String thongTinDiaChi;

    private Boolean laMacDinh;
}