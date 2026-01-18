package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "set_lau")
public class SetLau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_set_lau", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_set")
    private LoaiSetLau idLoaiSet;

    @Size(max = 50)
    @Column(name = "ma_set_lau", length = 50, insertable = false, updatable = false)
    private String maSetLau;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_set_lau", length = 200)
    private String tenSetLau;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "gia_ban", precision = 18)
    private BigDecimal giaBan;

    @Nationalized
    @Lob
    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Nationalized
    @Lob
    @Column(name = "mo_ta_chi_tiet")
    private String moTaChiTiet;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_sua")
    private Instant ngaySua;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_tao", length = 100)
    private String nguoiTao;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_sua", length = 100)
    private String nguoiSua;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idSetLau")
    private Set<ChiTietHoaDon> chiTietHoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "setLau", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ChiTietSetLau> listChiTietSetLau;

}