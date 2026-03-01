package com.example.datn_cozypot_spring_boot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePhieuDatBanFullResponse {
    private Integer idPhieuDatBan;
    private String maPhieuDatBan;
    private boolean daGuiMail;      // true/false
    private String emailGuiToi;
}
