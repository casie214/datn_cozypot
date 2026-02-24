package com.example.datn_cozypot_spring_boot.dto.DinhLuong;

import com.example.datn_cozypot_spring_boot.dto.DonVi.DinhLuongSimpleResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DinhLuongRequest {
    @NotBlank(message = "Tên đơn vị không được để trống")
    private String tenDonVi;  // VD: "ml", "L", "kg"

    private String moTa;      // VD: "Dùng cho đồ uống"

    // Danh sách các giá trị con gửi kèm
    private List<DinhLuongSimpleResponse> values;
}