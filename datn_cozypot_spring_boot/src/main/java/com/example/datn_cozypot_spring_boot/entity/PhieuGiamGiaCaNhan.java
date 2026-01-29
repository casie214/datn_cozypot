package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_giam_gia_ca_nhan", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_thanh_toan")
    private HoaDonThanhToan idHoaDonThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia idPhieuGiamGia;

    @Column(name = "trang_thai_su_dung")
    private Integer trangThaiSuDung;

    @Column(name = "ngay_het_han")
    private OffsetDateTime ngayHetHan;

    @Column(name = "ngay_nhan")
    private OffsetDateTime ngayNhan;

    @Column(name = "ngay_su_dung")
    private OffsetDateTime ngaySuDung;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguon_goc", length = 100)
    private String nguonGoc;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

}