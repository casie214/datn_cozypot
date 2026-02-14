package com.example.datn_cozypot_spring_boot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BanTrangThaiResponse {
        private Integer banId;
        private Integer trangThai; // 0-1-2 (TRẠNG THÁI BÀN)
}
