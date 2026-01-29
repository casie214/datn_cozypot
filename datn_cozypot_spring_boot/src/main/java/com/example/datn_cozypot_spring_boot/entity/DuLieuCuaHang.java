package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "du_lieu_cua_hang")
public class DuLieuCuaHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_du_lieu", nullable = false)
    private Integer id;

    @ColumnDefault("1")
    @Column(name = "do_uu_tien")
    private Integer doUuTien;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_cap_nhat")
    private OffsetDateTime ngayCapNhat;

    @Size(max = 200)
    @Nationalized
    @Column(name = "nhom_chu_de", length = 200)
    private String nhomChuDe;

    @Size(max = 200)
    @Nationalized
    @Column(name = "tu_khoa", length = 200)
    private String tuKhoa;

    @Nationalized
    @Lob
    @Column(name = "cau_hoi_mau")
    private String cauHoiMau;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "cau_tra_loi", nullable = false)
    private String cauTraLoi;

}