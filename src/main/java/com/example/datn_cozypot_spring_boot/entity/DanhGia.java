package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_danh_gia", nullable = false)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("isnull(CONVERT([varchar](10), 'DG'+right('0000'+CONVERT([varchar](10), [id_danh_gia]), 4)), '')")
    @Column(name = "ma_danh_gia", nullable = false, length = 10)
    private String maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDonThanhToan idHoaDon;

    @Column(name = "so_sao")
    private Integer soSao;

    @Nationalized
    @Lob
    @Column(name = "noi_dung")
    private String noiDung;

    @ColumnDefault("getdate()")
    @Column(name = "thoi_gian_danh_gia")
    private LocalDateTime thoiGianDanhGia;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idDanhGia")
    private Set<HinhAnhDanhGia> hinhAnhDanhGias = new LinkedHashSet<>();

}