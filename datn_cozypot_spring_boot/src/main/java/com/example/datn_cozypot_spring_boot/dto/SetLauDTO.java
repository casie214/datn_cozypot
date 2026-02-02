package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class SetLauDTO {
    private Integer id;
    private String maSetLau;
    private String tenSetLau;
    private BigDecimal giaBan;
    private String hinhAnh;
    private Integer trangThai;
}