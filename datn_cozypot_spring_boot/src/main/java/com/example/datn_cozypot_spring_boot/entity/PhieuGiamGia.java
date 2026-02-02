package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_giam_gia", nullable = false)
    private Integer id;

    // Cột mã phiếu được sinh tự động trong DB (PERSISTED),
    // nên để insertable = false, updatable = false
    @Column(name = "ma_phieu_giam_gia", insertable = false, updatable = false)
    private String maPhieuGiamGia;

    @Size(max = 50)
    @Column(name = "code_giam_gia", unique = true)
    private String codeGiamGia;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ten_phieu_giam_gia", length = 200)
    private String tenPhieuGiamGia;

    @Column(name = "loai_giam_gia")
    private Integer loaiGiamGia;

    // DB là DECIMAL(18,0), trong Java nên dùng BigDecimal hoặc Long
    @Column(name = "gia_tri_giam", precision = 18)
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_giam_toi_da", precision = 18)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "don_hang_toi_thieu", precision = 18)
    private BigDecimal donHangToiThieu;

    @Column(name = "doi_tuong")
    private Integer doiTuong;

    // SQL dùng DATETIME nên dùng LocalDateTime trong Java sẽ đồng bộ hơn Instant
    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_tao", length = 100)
    private String nguoiTao;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_sua", length = 100)
    private String nguoiSua;

    // Tự động gán ngày tạo khi lưu
    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dot_khuyen_mai")
    private DotKhuyenMai dotKhuyenMai; // Kiểu dữ liệu phải là DotKhuyenMai, không phải Integer

    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    private List<PhieuGiamGiaCaNhan> danhSachCaNhan;


}