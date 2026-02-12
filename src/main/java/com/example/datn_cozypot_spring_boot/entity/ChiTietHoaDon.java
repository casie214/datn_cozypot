package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_hoa_don")
public class ChiTietHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet_hd", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDonThanhToan idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_set_lau")
    private SetLau idSetLau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_mon_an")
    private ChiTietMonAn idChiTietMonAn;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_gia_tai_thoi_diem_ban", precision = 18)
    private BigDecimal donGiaTaiThoiDiemBan;

    @Column(name = "thanh_tien", precision = 18)
    private BigDecimal thanhTien;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu_mon")
    private String ghiChuMon;

    @Column(name = "trang_thai_mon")
    private Integer trangThaiMon;

    @Column(name = "ngay_gio_tao")
    private LocalDateTime ngayGioTao;

    @Column(name = "ngay_gio_sua")
    private Instant ngayGioSua;

}