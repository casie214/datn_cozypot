package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKhuyenMaiMonId implements Serializable {
    @Column(name = "id_dot_khuyen_mai")
    private Integer idDotKhuyenMai;

    @Column(name = "id_danh_muc_chi_tiet")
    private Integer idDanhMucChiTiet;
}