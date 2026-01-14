package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "giao_ca")
public class GiaoCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giao_ca", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @Column(name = "tien_mat_ca_truoc", precision = 18)
    private BigDecimal tienMatCaTruoc;

    @Column(name = "tien_mat_trong_ca", precision = 18)
    private BigDecimal tienMatTrongCa;

    @Column(name = "tien_ck_trong_ca", precision = 18)
    private BigDecimal tienCkTrongCa;

    @Column(name = "tong_tien_trong_ca", precision = 18)
    private BigDecimal tongTienTrongCa;

    @Column(name = "tien_mat_thuc_te", precision = 18)
    private BigDecimal tienMatThucTe;

    @Column(name = "tong_tien_phat_sinh", precision = 18)
    private BigDecimal tongTienPhatSinh;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai;

}