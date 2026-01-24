package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nhan_vien", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vai_tro")
    private VaiTro idVaiTro;

    @Column(name = "ma_nhan_vien", insertable = false, updatable = false)
    private String maNhanVien;

    @Nationalized
    @Column(name = "ho_ten_nhan_vien", length = 100)
    private String hoTenNhanVien;

    @Column(name = "sdt_nhan_vien", length = 20)
    private String sdtNhanVien;

    // --- Các trường mới cập nhật ---
    @Column(name = "so_cccd", length = 20, unique = true)
    private String soCccd;

    @Column(name = "ngay_cap_cccd")
    private LocalDate ngayCapCccd;

    @Nationalized
    @Column(name = "noi_cap_cccd")
    private String noiCapCccd;

    @Column(name = "anh_dai_dien", length = 500)
    private String anhDaiDien;
    // ------------------------------

    @Column(name = "ten_dang_nhap", length = 50, unique = true)
    private String tenDangNhap;

    @Column(name = "mat_khau_dang_nhap")
    private String matKhauDangNhap;

    @Column(name = "trang_thai_lam_viec")
    private Integer trangThaiLamViec;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam;

    @Nationalized
    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @OneToMany(mappedBy = "idNhanVien")
    private Set<GiaoCa> giaoCas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idNhanVien")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idNhanVien")
    private Set<LichLamViec> lichLamViecs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idNhanVien")
    private Set<LichSuHoaDon> lichSuHoaDons = new LinkedHashSet<>();

}