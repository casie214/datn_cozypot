package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_khach_hang", nullable = false)
    private Integer id;

    @Column(name = "ma_khach_hang", insertable = false, updatable = false)
    private String maKhachHang;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_khach_hang", length = 100)
    private String tenKhachHang;

    @Size(max = 20)
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @ColumnDefault("0")
    @Column(name = "diem_tich_luy")
    private Integer diemTichLuy;

    @Column(name = "ngay_tao_tai_khoan")
    private Instant ngayTaoTaiKhoan;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Size(max = 50)
    @Column(name = "ten_dang_nhap", length = 50)
    private String tenDangNhap;

    @Size(max = 255)
    @Column(name = "mat_khau_dang_nhap")
    private String matKhauDangNhap;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Size(max = 255)
    @Nationalized
    @Column(name = "dia_chi")
    private String diaChi;

    @JsonBackReference
    @OneToMany(mappedBy = "idKhachHang")
    private Set<DanhGia> danhGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<PhieuDatBan> phieuDatBans = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "khachHang")
    private List<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans;

    //Lấy thời gian thực tế lúc tạo
    @PrePersist
    protected void onCreate() {
        // Instant.now() sẽ khớp với kiểu dữ liệu Instant của trường ngayTaoTaiKhoan
        this.ngayTaoTaiKhoan = Instant.now();
    }

}