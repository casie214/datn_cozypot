package com.example.datn_cozypot_spring_boot.dto.monAn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MonAnResponse {
    private Integer id;

    private Integer idDanhMucChiTiet;
    private String tenDanhMucChiTiet;

    private String tenDanhMuc;

    private String maMonAn;
    private String tenMonAn;
    private String moTa;
    private Integer trangThaiKinhDoanh;
    private BigDecimal giaBan;

    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;

    private BigDecimal giaThapNhat;
    private BigDecimal giaCaoNhat;
    private String hinhAnh;
}
