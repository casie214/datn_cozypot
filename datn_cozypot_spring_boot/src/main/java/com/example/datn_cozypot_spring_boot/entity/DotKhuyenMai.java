package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Getter
@Setter
@Entity
@Table(name = "dot_khuyen_mai")
public class DotKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dot_khuyen_mai", nullable = false)
    private Integer id;

    @Column(name = "ma_dot_khuyen_mai", insertable = false, updatable = false)
    private String maDotKhuyenMai;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_dot_khuyen_mai", length = 200)
    private String tenDotKhuyenMai;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "phan_tram_giam", nullable = false)
    private Integer phanTramGiam;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao", updatable = false)
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

    @ManyToMany
    @JoinTable(
            name = "chi_tiet_khuyen_mai_set",
            joinColumns = @JoinColumn(name = "id_dot_khuyen_mai"),
            inverseJoinColumns = @JoinColumn(name = "id_set_lau")
    )
    private Set<SetLau> setLaus = new HashSet<>();

    // CHỈ GIỮ LẠI DUY NHẤT ĐOẠN NÀY
    @OneToMany(mappedBy = "dotKhuyenMai", cascade = CascadeType.ALL)
    private List<PhieuGiamGia> phieuGiamGias;

    @ManyToMany
    @JoinTable(
            name = "chi_tiet_khuyen_mai_mon",
            joinColumns = @JoinColumn(name = "id_dot_khuyen_mai"),
            inverseJoinColumns = @JoinColumn(name = "id_mon_an_di_kem")
    )
    private Set<DanhMucChiTiet> monAnDiKems = new HashSet<>();
}