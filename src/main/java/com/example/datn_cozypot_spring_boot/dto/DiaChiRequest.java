package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DiaChiRequest {
    private Integer id;
    private String hoTenNhan;
    private String soDienThoaiNhan;

    // Thêm các trường này để hứng dữ liệu từ Select2
    private String idTinhThanh;
    private String idQuanHuyen;
    private String idPhuongXa;
    private String diaChiChiTiet; // Đây là số nhà/tên đường
    private Boolean laMacDinh;
}