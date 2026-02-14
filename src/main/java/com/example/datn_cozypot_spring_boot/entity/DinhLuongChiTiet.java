package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dinh_luong_chi_tiet")
@Data
public class DinhLuongChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dinh_luong_chi_tiet")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_dinh_luong")
    private DinhLuong dinhLuong;
}