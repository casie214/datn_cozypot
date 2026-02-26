package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "khu_vuc")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhuVuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_khu_vuc", nullable = false)
    private Integer id;

    @Column(name = "ma_khu_vuc", insertable = false, updatable = false)
    private String maKhuVuc;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_khu_vuc", nullable = false, length = 100)
    private String tenKhuVuc;

    @Column(name = "tang")
    private Integer tang;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta")
    private String moTa;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idKhuVuc")
    @JsonIgnore
    private Set<BanAn> banAns = new LinkedHashSet<>();

}