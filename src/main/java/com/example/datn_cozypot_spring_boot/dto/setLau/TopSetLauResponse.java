package com.example.datn_cozypot_spring_boot.dto.setLau;

import com.example.datn_cozypot_spring_boot.entity.SetLau;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSetLauResponse {
    private SetLau setLau;
    private Long tongSoLuongBan;
}