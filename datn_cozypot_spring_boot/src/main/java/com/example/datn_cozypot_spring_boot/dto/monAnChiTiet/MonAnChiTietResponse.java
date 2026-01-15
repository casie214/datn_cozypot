package com.example.datn_cozypot_spring_boot.dto.monAnChiTiet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonAnChiTietResponse {
    private Integer id;

    private Integer idMonAnDiKem;
    private String tenMonAn;

    private String maChiTietMonAn;
    private String tenChiTietMonAn;

    private BigDecimal giaBan;
    private BigDecimal giaVon;

    private String kichCo;
    private String donVi;

    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;
}
