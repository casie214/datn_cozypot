package com.example.datn_cozypot_spring_boot.dto.loaiLau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoaiLauRequest {
    @NotBlank(message = "Mã loại set không được để trống")
    @Size(max = 50, message = "Mã loại set không được quá 50 ký tự")
    private String maLoaiSet;

    @NotBlank(message = "Tên loại set không được để trống")
    @Size(max = 100, message = "Tên loại set không được quá 100 ký tự")
    private String tenLoaiSet;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String moTa;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai; // 1: Hoạt động, 0: Ngưng hoạt động
}
