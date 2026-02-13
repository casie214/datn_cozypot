package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dinh_luong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DinhLuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dinh_luong")
    private Integer id;

    @Column(name = "ten_hien_thi")
    private String tenHienThi;

    @Column(name = "kich_co")
    private String kichCo;

    @Column(name = "dinh_luong")
    private String dinhLuong;
}