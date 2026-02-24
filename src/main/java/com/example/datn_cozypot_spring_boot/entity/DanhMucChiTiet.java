package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "danh_muc_chi_tiet")
public class DanhMucChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_danh_muc_chi_tiet")
    private Integer id;

    @Column(name = "ma_danh_muc_chi_tiet")
    private String maMon;

    @Column(name = "ten_danh_muc_chi_tiet")
    private String tenMon;

    @Column(name = "gia_ban")
    private BigDecimal giaBan;

    @Column(name = "gia_von")
    private BigDecimal giaVon;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hinh_anh_single", columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnh;

    @Column(name = "trang_thai")
    private Integer trangThai;

    // --- Relationships ---
    @ManyToOne
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dinh_luong")
    private DinhLuong dinhLuong;

    // --- Audit Fields ---
    @CreationTimestamp
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;
    @UpdateTimestamp
    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;
    @Column(name = "nguoi_tao")
    private String nguoiTao;
    @Column(name = "nguoi_sua")
    private String nguoiSua;
}