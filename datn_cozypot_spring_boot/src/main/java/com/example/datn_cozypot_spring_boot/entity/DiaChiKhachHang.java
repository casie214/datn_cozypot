package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "dia_chi_khach_hang")
@Getter @Setter // Hoặc @Data nếu dùng Lombok
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dia_chi") // Khớp với SQL của bạn
    private Integer id; // Đặt tên là id để khớp với code trong Service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "ho_ten_nhan")
    private String hoTenNhan;

    @Column(name = "so_dien_thoai_nhan")
    private String soDienThoaiNhan;

    @Column(name = "thong_tin_dia_chi")
    private String thongTinDiaChi;

    @Column(name = "la_mac_dinh")
    private Boolean laMacDinh;
}