package com.example.datn_cozypot_spring_boot.dto.monAnChiTiet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonAnChiTietRequest {

    @NotNull(message = "ID món ăn đi kèm không được để trống")
    private Integer idMonAnDiKem;

    @NotBlank(message = "Tên chi tiết món ăn không được để trống")
    @Size(max = 200, message = "Tên chi tiết không được quá 200 ký tự")
    private String tenChiTietMonAn;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;

    @Min(value = 0, message = "Giá vốn phải lớn hơn hoặc bằng 0")
    private BigDecimal giaVon;

    @Size(max = 50, message = "Kích cỡ không được quá 50 ký tự")
    private String kichCo;

    @Size(max = 50, message = "Đơn vị không được quá 50 ký tự")
    private String donVi;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    private String hinhAnh;
}