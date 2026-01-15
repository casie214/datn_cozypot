package com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DanhMucChiTietRequest {
    @NotNull(message = "ID danh mục cha không được để trống")
    private Integer idDanhMuc; // Chỉ nhận ID của danh mục cha

    @Size(max = 50, message = "Mã chi tiết không được quá 50 ký tự")
    private String maDanhMucChiTiet;

    @NotBlank(message = "Tên danh mục chi tiết không được để trống")
    @Size(max = 100, message = "Tên danh mục chi tiết không được quá 100 ký tự")
    private String tenDanhMucChiTiet;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String moTa;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
