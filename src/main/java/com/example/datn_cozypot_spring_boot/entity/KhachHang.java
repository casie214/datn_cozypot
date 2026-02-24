package com.example.datn_cozypot_spring_boot.entity;

import com.example.datn_cozypot_spring_boot.config.AuthProvider;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

    // --- Bổ sung các trường mới ---

    // Mở rộng lên 500 để lưu tên file ảnh thoải mái
    @Size(max = 500)
    @Column(name = "anh_dai_dien", length = 500)
    private String anhDaiDien;
    // Thêm vào bên trong class KhachHang
    @Column(name = "trang_thai")
    private Integer trangThai;
    // PHẢI SỬA: Nâng từ 50 lên 100 để khớp với SQL bạn vừa chạy
    @Size(max = 100)
    @Column(name = "ten_dang_nhap", length = 100)
    private String tenDangNhap;

    @Size(max = 255)
    @Column(name = "mat_khau_dang_nhap", length = 255)
    private String matKhauDangNhap;

    // Đảm bảo authProvider map đúng cột trong SQL
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 50)
    private AuthProvider authProvider;

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

    @PrePersist
    protected void onCreate() {
        this.ngayTaoTaiKhoan = Instant.now();
        if (this.diemTichLuy == null) {
            this.diemTichLuy = 0;
        }
    }

    // Sửa lại dòng khai báo trong KhachHang.java
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DiaChiKhachHang> danhSachDiaChi = new ArrayList<>(); // Thêm khởi tạo này
}