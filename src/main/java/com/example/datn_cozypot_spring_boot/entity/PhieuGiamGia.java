package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_giam_gia", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dot_khuyen_mai")
    private DotKhuyenMai idDotKhuyenMai;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_phieu_giam_gia", length = 200)
    private String tenPhieuGiamGia;

    @Column(name = "loai_giam_gia")
    private Integer loaiGiamGia;

    @Column(name = "gia_tri_giam", precision = 18)
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_giam_toi_da", precision = 18)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "don_hang_toi_thieu", precision = 18)
    private BigDecimal donHangToiThieu;

    @Column(name = "ngay_bat_dau")
    private Instant ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Instant ngayKetThuc;

    @Column(name = "so_luong_phat_hanh")
    private Integer soLuongPhatHanh;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung;

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

    @OneToMany(mappedBy = "idPhieuGiamGia")
    private Set<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans = new LinkedHashSet<>();

}