package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PhieuGiamGiaDTO {
    private String codeGiamGia;
    private String tenPhieuGiamGia;
    private Integer loaiGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriGiamToiDa;
    private BigDecimal donHangToiThieu;
    private Integer doiTuong;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer soLuong;
    private Integer trangThai;

    // QUAN TRỌNG: Thêm 2 trường này để nhận dữ liệu từ Vue
    private List<Integer> listIdKhachHang;
    private List<String> listEmails;

    private Integer idDotKhuyenMai;





}