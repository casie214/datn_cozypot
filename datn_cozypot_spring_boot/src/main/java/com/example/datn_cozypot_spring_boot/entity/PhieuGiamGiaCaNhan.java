package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_giam_gia_ca_nhan", nullable = false)
    private Integer id;

<<<<<<< HEAD
    @JsonBackReference
    @ManyToOne
=======
    // Cột computed trong SQL Server → chỉ để read
    @Column(name = "ma_giam_gia_ca_nhan", insertable = false, updatable = false)
    private String maGiamGiaCaNhan;

    @ManyToOne(fetch = FetchType.LAZY)
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia phieuGiamGia;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_thanh_toan")
    private HoaDonThanhToan idHoaDonThanhToan;

    @Column(name = "ngay_nhan")
    private Instant ngayNhan;

    @Column(name = "ngay_su_dung")
    private Instant ngaySuDung;

    @Column(name = "trang_thai_su_dung")
    private Integer trangThaiSuDung;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguon_goc", length = 100)
    private String nguonGoc;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ngay_het_han")
    private Instant ngayHetHan;
}