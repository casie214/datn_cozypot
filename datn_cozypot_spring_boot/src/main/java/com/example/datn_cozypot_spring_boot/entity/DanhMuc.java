package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "danh_muc")
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_danh_muc", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Column(name = "ma_danh_muc", length = 50)
    private String maDanhMuc;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String tenDanhMuc;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("getdate()")
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

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "danhMuc")
    private Set<DanhMucChiTiet> danhMucChiTiets = new LinkedHashSet<>();

    @Formula("(SELECT COUNT(c.id_danh_muc_chi_tiet) FROM danh_muc_chi_tiet c WHERE c.id_danh_muc = id_danh_muc)")
    private Integer soLuongMon;

    @ManyToMany(mappedBy = "listDanhMuc")
    @JsonIgnore // Tránh loop JSON
    private List<DonVi> listDonVi;
}