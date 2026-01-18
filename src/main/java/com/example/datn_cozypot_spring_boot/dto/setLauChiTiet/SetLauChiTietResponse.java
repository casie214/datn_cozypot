package com.example.datn_cozypot_spring_boot.dto.setLauChiTiet;

import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetLauChiTietResponse {
    private Integer id;
    private Integer soLuong;
    private MonAnChiTietResponse chiTietMonAn;
}
