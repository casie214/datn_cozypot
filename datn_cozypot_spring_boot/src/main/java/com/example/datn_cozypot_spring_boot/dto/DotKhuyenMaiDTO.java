package com.example.datn_cozypot_spring_boot.dto;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DotKhuyenMaiDTO {
    private Integer id;
    private String maDotKhuyenMai;
    private String tenDotKhuyenMai;
    private String moTa;
    private Integer phanTramGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;
    private List<Integer> idSetLauChiTiet; // Danh sách ID các Set Lẩu được chọn
    private List<Integer> idMonAnChiTiet; // Thêm dòng này: Danh sách ID các món ăn lẻ được chọn
}
=======
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
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
