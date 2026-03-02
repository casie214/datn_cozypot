package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tham_so_he_thong",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_ma_tham_so", columnNames = "ma_tham_so")
        }
)
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
    @Column(name = "ten_tham_so", nullable = false, length = 100)
    private String tenThamSo;

    @Size(max = 255)
    @NotNull
    @Column(name = "gia_tri", nullable = false, length = 255)
    private String giaTri;

    @Size(max = 50)
    @Column(name = "kieu_du_lieu", length = 50)
    private String kieuDuLieu;

    @Size(max = 255)
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    // 🔥 Tự động set ngày khi insert/update
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.ngayCapNhat = Instant.now();
        if (this.trangThai == null) {
            this.trangThai = 1;
        }
    }
}