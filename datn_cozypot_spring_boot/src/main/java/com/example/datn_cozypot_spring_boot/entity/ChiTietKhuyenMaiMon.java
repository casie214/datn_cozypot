package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_khuyen_mai_mon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKhuyenMaiMon {

    @EmbeddedId
    private ChiTietKhuyenMaiMonId id;

    @ManyToOne
    @MapsId("idDotKhuyenMai")
    @JoinColumn(name = "id_dot_khuyen_mai")
    private DotKhuyenMai dotKhuyenMai;

    @ManyToOne
    @MapsId("idDanhMucChiTiet")
    @JoinColumn(name = "id_danh_muc_chi_tiet")
    private DanhMucChiTiet danhMucChiTiet;
}