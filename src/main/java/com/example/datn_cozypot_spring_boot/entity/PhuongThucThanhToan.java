package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phuong_thuc", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_phuong_thuc", length = 100)
    private String tenPhuongThuc;

    @Size(max = 50)
    @Column(name = "ma_phuong_thuc", length = 50)
    private String maPhuongThuc;

    @Nationalized
    @Lob
    @Column(name = "url_anh_phuong_thuc")
    private String urlAnhPhuongThuc;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idPhuongThucThanhToan")
    private Set<LichSuThanhToan> lichSuThanhToans = new LinkedHashSet<>();

}