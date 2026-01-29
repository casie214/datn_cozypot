package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "mon_an_di_kem")
public class MonAnDiKem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mon_an_di_kem", nullable = false)
    private Integer id;

    @Column(name = "gia_ban", precision = 18)
    private BigDecimal giaBan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc_chi_tiet")
    private DanhMucChiTiet idDanhMucChiTiet;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "trang_thai_kinh_doanh")
    private Integer trangThaiKinhDoanh;

    @Column(name = "ngay_sua")
    private OffsetDateTime ngaySua;

    @Column(name = "ngay_tao")
    private OffsetDateTime ngayTao;

    @Size(max = 50)
    @Column(name = "ma_mon_an", length = 50)
    private String maMonAn;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_sua", length = 100)
    private String nguoiSua;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_tao", length = 100)
    private String nguoiTao;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_mon_an", length = 200)
    private String tenMonAn;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @OneToMany(mappedBy = "idMonAnDiKem")
    private Set<ChiTietMonAn> chiTietMonAns = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idMonAnDiKem")
    private Set<HinhAnhMonAn> hinhAnhMonAns = new LinkedHashSet<>();

}