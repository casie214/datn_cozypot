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

    private Boolean gioiTinh;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate ngaySinh;

    @NotBlank(message = "Vui lòng chọn Tỉnh/Thành phố")
    private String idTinhThanh;

    @NotBlank(message = "Vui lòng chọn Quận/Huyện")
    private String idQuanHuyen;

    @NotBlank(message = "Vui lòng chọn Phường/Xã")
    private String idPhuongXa;
    private String tenTinhThanh;
    private String tenQuanHuyen;
    private String tenPhuongXa;
    @NotBlank(message = "Vui lòng nhập địa chỉ chi tiết")
    private String diaChiChiTiet;

    private String hoTenNhan;
    private String soDienThoaiNhan;
    private Boolean laMacDinh;
}