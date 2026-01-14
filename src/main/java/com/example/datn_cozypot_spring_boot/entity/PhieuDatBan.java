package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "phieu_dat_ban")
public class PhieuDatBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_dat_ban", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ban_an")
    private BanAn idBanAn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @Column(name = "thoi_gian_dat")
    private Instant thoiGianDat;

    @Column(name = "hinh_thuc_dat")
    private Integer hinhThucDat;

    @Column(name = "so_luong_khach")
    private Integer soLuongKhach;

    @Size(max = 50)
    @Column(name = "ma_nhan_ban", length = 50)
    private String maNhanBan;

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

    @OneToMany(mappedBy = "idPhieuDatBan")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

}