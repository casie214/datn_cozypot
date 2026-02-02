package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lich_su_hoa_don", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDonThanhToan idHoaDon;

    @Size(max = 255)
    @Nationalized
    @Column(name = "hanh_dong")
    private String hanhDong;

    @Column(name = "thoi_gian_thuc_hien")
    private Instant thoiGianThucHien;

    @Nationalized
    @Lob
    @Column(name = "ly_do_thuc_hien")
    private String lyDoThucHien;

    @Column(name = "trang_thai_truoc_do")
    private Integer trangThaiTruocDo;

    @Column(name = "trang_thai_moi")
    private Integer trangThaiMoi;

}