package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "loai_set_lau")
public class LoaiSetLau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loai_set", nullable = false)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("isnull(CONVERT([varchar](10), 'SL'+right('000'+CONVERT([varchar](10), [id_loai_set]), 3)), '')")
    @Column(name = "ma_loai_set", nullable = false, length = 10)
    private String maLoaiSet;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten_loai_set", length = 100)
    private String tenLoaiSet;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
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

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idLoaiSet")
    private Set<SetLau> setLaus = new LinkedHashSet<>();

}