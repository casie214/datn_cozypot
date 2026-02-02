package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @Column(name = "id_danh_muc_chi_tiet", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc idDanhMuc;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("isnull(CONVERT([varchar](10), 'DCT'+right('000'+CONVERT([varchar](10), [id_danh_muc_chi_tiet]), 3)), '')")
    @Column(name = "ma_danh_muc_chi_tiet", nullable = false, length = 10)
    private String maDanhMucChiTiet;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_danh_muc_chi_tiet", length = 100)
    private String tenDanhMucChiTiet;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

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

    @OneToMany(mappedBy = "idDanhMucChiTiet")
    private Set<MonAnDiKem> monAnDiKems = new LinkedHashSet<>();

}