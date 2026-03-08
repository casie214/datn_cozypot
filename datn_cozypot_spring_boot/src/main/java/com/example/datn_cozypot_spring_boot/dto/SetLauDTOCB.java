package com.example.datn_cozypot_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SetLauDTOCB {
    private String tenSet;
    private String thuocLoai;
    private BigDecimal gia;
    private String moTa;
    private List<String> cacMonTrongSet;
}
