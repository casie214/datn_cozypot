package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data

public class DotKhuyenMaiDTO {
    private Integer id;

    // Cột computed – chỉ để hiển thị
    private String maDotKhuyenMai;

    @Size(max = 200, message = "Tên đợt khuyến mãi tối đa 200 ký tự")
    private String tenDotKhuyenMai;

    private String moTa;

    private Integer loaiKhuyenMai;

    @Size(max = 200)
    private String doiTuongApDung;

    @Size(max = 100)
    private String khungGioApDung;

    private String danhSachMonApDung;

    private Integer trangThai;

    private Instant ngayTao;

    private Instant ngaySua;

    @Size(max = 100)
    private String nguoiTao;

    @Size(max = 100)
    private String nguoiSua;
}
