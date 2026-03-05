package com.example.datn_cozypot_spring_boot.dto.profile;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NhanVienProfileRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String hoTenNhanVien;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0")
    private String sdtNhanVien;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, max = 50, message = "Tên đăng nhập phải từ 5 đến 50 ký tự")
    private String tenDangNhap;

    @NotNull(message = "Vui lòng chọn ngày sinh")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate ngaySinh;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String diaChi;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gioiTinh;

    // CCCD
    @Size(max = 20, message = "Số CCCD không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[0-9]*$", message = "Số CCCD chỉ được chứa các ký số")
    private String soCccd;

    @PastOrPresent(message = "Ngày cấp CCCD không được là ngày trong tương lai")
    private LocalDate ngayCapCccd;

    @Size(max = 255, message = "Nơi cấp CCCD không được vượt quá 255 ký tự")
    private String noiCapCccd;

    private String anhDaiDien;
}
