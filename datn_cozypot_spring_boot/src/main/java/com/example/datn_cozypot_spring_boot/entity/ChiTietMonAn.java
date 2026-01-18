package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_mon_an")
public class ChiTietMonAn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet_mon_an", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mon_an_di_kem")
    private MonAnDiKem idMonAnDiKem;

    @Size(max = 50)
    @Column(name = "ma_chi_tiet_mon_an", length = 50, insertable = false, updatable = false)
    private String maChiTietMonAn;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_chi_tiet_mon_an", length = 200)
    private String tenChiTietMonAn;

    @Column(name = "gia_ban", precision = 18)
    private BigDecimal giaBan;

    @Column(name = "gia_von", precision = 18)
    private BigDecimal giaVon;

    @Size(max = 50)
    @Nationalized
    @Column(name = "kich_co", length = 50)
    private String kichCo;

    @Size(max = 50)
    @Nationalized
    @Column(name = "don_vi", length = 50)
    private String donVi;

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

    @OneToMany(mappedBy = "idChiTietMonAn")
    private Set<ChiTietHoaDon> chiTietHoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chiTietMonAn")
    @JsonIgnore
    private List<ChiTietSetLau> cacSetLauChuaMonNay;

    @Column(name = "url_hinh_anh", columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnh;

}