package com.example.datn_cozypot_spring_boot.config;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Tên người dùng không được để trống")
    private String tenDangNhap;

    @NotBlank(message = "Họ tên không được để trống")
    private String tenKhachHang;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhauDangNhap;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

    private String diaChi;

    private Boolean gioiTinh;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate ngaySinh;
}
