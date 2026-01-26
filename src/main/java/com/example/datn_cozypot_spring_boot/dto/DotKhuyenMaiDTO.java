package com.example.datn_cozypot_spring_boot.dto;

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