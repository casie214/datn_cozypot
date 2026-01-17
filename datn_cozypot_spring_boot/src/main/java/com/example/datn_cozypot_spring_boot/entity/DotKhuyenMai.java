package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "dot_khuyen_mai")
public class DotKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dot_khuyen_mai", nullable = false)
    private Integer id;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_dot_khuyen_mai", length = 200)
    private String tenDotKhuyenMai;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "loai_khuyen_mai")
    private Integer loaiKhuyenMai;

    @Size(max = 200)
    @Nationalized
    @Column(name = "doi_tuong_ap_dung", length = 200)
    private String doiTuongApDung;

    @Size(max = 100)
    @Nationalized
    @Column(name = "khung_gio_ap_dung", length = 100)
    private String khungGioApDung;

    @Nationalized
    @Lob
    @Column(name = "danh_sach_mon_ap_dung")
    private String danhSachMonApDung;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

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

    @OneToMany(mappedBy = "idDotKhuyenMai")
    private Set<PhieuGiamGia> phieuGiamGias = new LinkedHashSet<>();

}