package com.example.datn_cozypot_spring_boot.dto.MonAn;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonAnRequest {
    @NotBlank(message = "Tên món không được để trống")
    private String tenMon;

    // Mã món có thể để null để backend tự sinh
    private String maMon;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn 0")
    private BigDecimal giaBan;

    private BigDecimal giaVon;
    private String moTa;
    private String hinhAnh;
    private Integer trangThai;

    @NotNull(message = "ID Danh mục là bắt buộc")
    private Integer idDanhMuc;

    @NotNull(message = "ID Định lượng là bắt buộc")
    private Integer idDinhLuong;
}
