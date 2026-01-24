package com.example.datn_cozypot_spring_boot.dto.setLau;

import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetLauResponse {
    private Integer id;

    private Integer idLoaiSet;
    private String tenLoaiSet;

    private String maSetLau;
    private String tenSetLau;
    private String moTa;
    private BigDecimal giaBan;
    private String hinhAnh;
    private String moTaChiTiet;

    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;

    private List<SetLauChiTietResponse> listChiTietSetLau;
}
