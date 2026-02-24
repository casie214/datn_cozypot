package com.example.datn_cozypot_spring_boot.dto.setLau;

import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietRequest;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
public class SetLauRequest {
    @NotBlank(message = "Tên set không được để trống")
    private String tenSetLau;

    private String maSetLau;

    @NotNull
    private BigDecimal giaBan;

    private String moTa;
    private String hinhAnh;
    private Integer idLoaiSet;
    private Integer trangThai;

    // Danh sách các món ăn trong set này
    @NotEmpty(message = "Set lẩu phải có ít nhất 1 món")
    private List<ChiTietSetItemRequest> chiTietMonAn;

    @Data
    public static class ChiTietSetItemRequest {
        private Integer idMonAn;
        private Integer soLuong;
    }
}




