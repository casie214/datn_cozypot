package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NhanVienRequest {

    @NotBlank(message = "Họ tên nhân viên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String hoTenNhanVien;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String sdtNhanVien;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, max = 50, message = "Tên đăng nhập phải từ 5 đến 50 ký tự")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhauDangNhap;

    @NotNull(message = "Vui lòng chọn trạng thái làm việc")
    private Integer trangThaiLamViec;

    @NotNull(message = "Vui lòng chọn ngày sinh")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate ngaySinh;

    @NotNull(message = "Vui lòng chọn ngày vào làm")
    private LocalDate ngayVaoLam;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String diaChi;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gioiTinh;

    @NotNull(message = "Vui lòng chọn vai trò nhân viên")
    private Integer idVaiTro;

    // --- Các trường định danh mới ---
    @Size(max = 20, message = "Số CCCD không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[0-9]*$", message = "Số CCCD chỉ được chứa các ký số")
    private String soCccd;

    @PastOrPresent(message = "Ngày cấp CCCD không được là ngày trong tương lai")
    private LocalDate ngayCapCccd;

    @Size(max = 255, message = "Nơi cấp CCCD không được vượt quá 255 ký tự")
    private String noiCapCccd;

    @Size(max = 500, message = "Đường dẫn ảnh quá dài")
    private String anhDaiDien;
}