package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ban_an")
@DynamicUpdate
public class BanAn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ban_an", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_khu_vuc")
    private KhuVuc idKhuVuc;

    @Size(max = 10)
    @ColumnDefault("isnull(CONVERT([varchar](10), 'BA'+right('000'+CONVERT([varchar](10), [id_ban_an]), 3)), '')")
    @Column(
            name = "ma_ban",
            insertable = false,
            updatable = false,
            length = 10
    )
    private String maBan;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_ban", length = 100)
    private String tenBan;

    @Column(name = "so_nguoi_toi_da")
    private Integer soNguoiToiDa;

    @ColumnDefault("0")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @ColumnDefault("1")
    @Column(name = "loai_dat_ban")
    private Integer loaiDatBan;

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

    @OneToMany(mappedBy = "idBanAn")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idBanAn")
    private Set<PhieuDatBan> phieuDatBans = new LinkedHashSet<>();

}