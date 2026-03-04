package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "dia_chi_khach_hang")
@Getter
@Setter
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dia_chi")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
    @Nationalized
    @Column(name = "ho_ten_nhan")
    private String hoTenNhan;

    @Column(name = "so_dien_thoai_nhan")
    private String soDienThoaiNhan;


    @Column(name = "la_mac_dinh")
    private Boolean laMacDinh;

    @Column(name = "id_tinh_thanh")
    private String idTinhThanh;

    @Column(name = "id_quan_huyen")
    private String idQuanHuyen;

    @Column(name = "id_phuong_xa")
    private String idPhuongXa;

    @Column(name = "dia_chi_chi_tiet", columnDefinition = "NVARCHAR(500)")
    private String diaChiChiTiet;


}