package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "hinh_anh_danh_gia")
public class HinhAnhDanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hinh_anh_danh_gi", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_gia")
    private DanhGia idDanhGia;

    @Nationalized
    @Lob
    @Column(name = "url_anh")
    private String urlAnh;

    @Column(name = "trang_thai")
    private Integer trangThai;

}