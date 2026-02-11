package com.example.datn_cozypot_spring_boot.dto.setLau;

import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SetLauRequest {

    @NotNull(message = "ID loại set lẩu không được để trống")
    private Integer idLoaiSet; // Chỉ nhận ID

    @NotBlank(message = "Tên set lẩu không được để trống")
    @Size(max = 200, message = "Tên set lẩu không được quá 200 ký tự")
    private String tenSetLau;

    private String moTa;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;

    private String hinhAnh;


    @NotNull(message = "Định mức không được để trống")
    private String moTaChiTiet;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    private List<SetLauChiTietRequest> listChiTietSetLau;
}