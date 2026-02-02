package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class KhachHangRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự")
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String soDienThoai;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotNull(message = "Vui lòng chọn ngày sinh")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate ngaySinh;

    @Min(value = 0, message = "Điểm tích lũy không được nhỏ hơn 0")
    private Integer diemTichLuy;

    private Instant ngayTaoTaiKhoan;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gioiTinh;

    @Size(max = 500, message = "Đường dẫn ảnh quá dài")
    private String anhDaiDien;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, max = 50, message = "Tên đăng nhập phải từ 5 đến 50 ký tự")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có nhất 6 ký tự")
    private String matKhauDangNhap;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private Integer trangThai;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String diaChi;

}