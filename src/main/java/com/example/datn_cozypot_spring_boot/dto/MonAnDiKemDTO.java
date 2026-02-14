package com.example.datn_cozypot_spring_boot.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class MonAnDiKemDTO {
    private Integer id;
    private String maMonAn;
    private String tenMonAn;
    private BigDecimal giaBan;
    private Integer trangThai;
    private String hinhAnh;

}