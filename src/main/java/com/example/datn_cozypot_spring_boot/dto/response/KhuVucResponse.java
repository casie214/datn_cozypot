package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.entity.KhuVuc;
import lombok.Data;

@Data
public class KhuVucResponse {
    private Integer id;
    private String tenKhuVuc;

    public KhuVucResponse(KhuVuc khuVuc) {
        this.id = khuVuc.getId();
        this.tenKhuVuc = khuVuc.getTenKhuVuc();
    }
}
