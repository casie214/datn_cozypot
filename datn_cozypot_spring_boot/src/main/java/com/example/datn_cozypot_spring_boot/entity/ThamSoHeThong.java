package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tham_so_he_thong")
public class ThamSoHeThong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tham_so", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_tham_so", nullable = false, length = 50)
    private String maThamSo;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_tham_so", nullable = false, length = 100)
    private String tenThamSo;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "gia_tri", nullable = false)
    private String giaTri;

    @Size(max = 50)
    @Column(name = "kieu_du_lieu", length = 50)
    private String kieuDuLieu;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

}