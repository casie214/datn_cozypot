package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "gia_tri", nullable = false)
    private String giaTri;

    @Column(name = "ten_hien_thi")
    private String tenHienThi;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_don_vi", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DonVi donVi;

    @Column(name = "trang_thai")
    private Integer trangThai;
}