package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ban_an")
public class BanAn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ban_an", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khu_vuc")
    private KhuVuc idKhuVuc;

    @Size(max = 50)
    @Column(name = "ma_ban", length = 50)
    private String maBan;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_ban", length = 100)
    private String tenBan;

    @Column(name = "so_nguoi_toi_da")
    private Integer soNguoiToiDa;

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

    @OneToMany(mappedBy = "idBanAn")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idBanAn")
    private Set<PhieuDatBan> phieuDatBans = new LinkedHashSet<>();

}