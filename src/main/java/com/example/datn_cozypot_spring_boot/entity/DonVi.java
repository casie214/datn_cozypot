package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "don_vi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonVi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_don_vi")
    private Integer id;

    @Column(name = "ten_don_vi", nullable = false)
    private String tenDonVi;

    @Column(name = "mo_ta")
    private String moTa;

    // QUAN HỆ 1-N VỚI GIÁ TRỊ CON (Giữ nguyên)
    @OneToMany(mappedBy = "donVi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DinhLuong> listDinhLuong = new ArrayList<>();

    // QUAN HỆ N-N VỚI DANH MỤC (MỚI)
    // Ý nghĩa: Đơn vị này được dùng cho những danh mục nào
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "don_vi_danh_muc",
            joinColumns = @JoinColumn(name = "id_don_vi"),
            inverseJoinColumns = @JoinColumn(name = "id_danh_muc")
    )
    private List<DanhMuc> listDanhMuc = new ArrayList<>();
}