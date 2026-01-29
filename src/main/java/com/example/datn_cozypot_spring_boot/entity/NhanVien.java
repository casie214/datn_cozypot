package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_vai_tro")
    private VaiTro idVaiTro;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("isnull(CONVERT([varchar](10), 'NV'+right('0000'+CONVERT([varchar](10), [id_nhan_vien]), 4)), '')")
    @Column(name = "ma_nhan_vien", nullable = false, length = 10)
    private String maNhanVien;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ho_ten_nhan_vien", length = 100)
    private String hoTenNhanVien;

    @Size(max = 20)
    @Column(name = "sdt_nhan_vien", length = 20)
    private String sdtNhanVien;

    @Size(max = 50)
    @Column(name = "ten_dang_nhap", length = 50)
    private String tenDangNhap;

    @Size(max = 255)
    @Column(name = "mat_khau_dang_nhap")
    private String matKhauDangNhap;

    @ColumnDefault("1")
    @Column(name = "trang_thai_lam_viec")
    private Integer trangThaiLamViec;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam;

    @Size(max = 255)
    @Nationalized
    @Column(name = "dia_chi")
    private String diaChi;

    @Size(max = 100)
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