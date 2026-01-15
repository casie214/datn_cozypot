package com.example.datn_cozypot_spring_boot.dto.danhMuc;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DanhMucRequest {

    @Size(max = 50, message = "Mã danh mục không được quá 50 ký tự")
    private String maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được quá 100 ký tự")
    private String tenDanhMuc;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String moTa;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
