package com.example.datn_cozypot_spring_boot.dto.monAn;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MonAnRequest {
    @NotNull(message = "ID danh mục chi tiết không được để trống")
    private Integer idDanhMucChiTiet;

    @NotBlank(message = "Mã món ăn không được để trống")
    @Size(max = 50, message = "Mã món ăn không được quá 50 ký tự")
    private String maMonAn;

    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 200, message = "Tên món ăn không được quá 200 ký tự")
    private String tenMonAn;

    private String moTa;

    @NotNull(message = "Trạng thái kinh doanh không được để trống")
    private Integer trangThaiKinhDoanh;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;

    private Integer trangThai;
}
