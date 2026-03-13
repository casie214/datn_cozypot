package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hoa_don_thanh_toan")
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HoaDonThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hoa_don", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(SELECT TOP 1 pb.id_ban_an FROM phieu_dat_ban_ban_an pb WHERE pb.id_phieu_dat_ban = id_phieu_dat_ban)")
    @NotFound(action = NotFoundAction.IGNORE)
    private BanAn idBanAn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_dat_ban")
    private PhieuDatBan idPhieuDatBan;

    @Size(max = 50)
    @Column(name = "ma_hoa_don", length = 50, insertable = false, updatable = false)
    private String maHoaDon;

    @Column(name = "thoi_gian_tao")
    private Instant thoiGianTao;

    @Column(name = "thoi_gian_thanh_toan")
    private Instant thoiGianThanhToan;

    @Column(name = "tong_tien_chua_giam", precision = 18)
    private BigDecimal tongTienChuaGiam;

    @Column(name = "so_tien_da_giam", precision = 18)
    private BigDecimal soTienDaGiam;

    @Column(name = "tien_coc", precision = 18)
    private BigDecimal tienCoc;

    @Column(name = "tien_hoan_tra", precision = 18)
    private BigDecimal tienHoanTra;

    @Column(name = "tong_tien_thanh_toan", precision = 18)
    private BigDecimal tongTienThanhToan;

    @Column(name = "vat_ap_dung", precision = 18)
    private BigDecimal vatApDung;

    @Column(name = "diem_su_dung")
    private Integer diemSuDung;

    @Column(name = "diem_cong_them")
    private Integer diemCongThem;

    @Column(name = "trang_thai_hoa_don")
    private Integer trangThaiHoaDon;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "tien_khach_dua", precision = 18)
    private BigDecimal tienKhachDua;

    @Column(name = "tien_thua", precision = 18)
    private BigDecimal tienThua;

    @OneToMany(mappedBy = "idHoaDon")
    @JsonIgnore
    private Set<ChiTietHoaDon> chiTietHoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idHoaDon")
    @JsonIgnore
    private Set<DanhGia> danhGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idHoaDon")
    @JsonIgnore
    private Set<LichSuHoaDon> lichSuHoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "HoaDon")
    @JsonIgnore
    private Set<LichSuThanhToan> lichSuThanhToans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idHoaDonThanhToan")
    @JsonIgnore
    private Set<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans = new LinkedHashSet<>();

}
