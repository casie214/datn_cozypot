package com.example.datn_cozypot_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuSummaryDTO {
    private List<CategoryDTOCB> danhMuc;
    private List<LoaiSetLauDTOCB> loaiSetLau;
    private List<SetLauDTOCB> setLau;
    private List<MonAnDTOCB> monAnLe;
}

