package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "vai_tro")
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vai_tro", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Column(name = "ma_vai_tro", insertable = false, updatable = false)
    private String maVaiTro;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_vai_tro", nullable = false, length = 100)
    private String tenVaiTro;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mo_ta_vai_tro")
    private String moTaVaiTro;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idVaiTro")
    @JsonIgnore

    private Set<NhanVien> nhanViens = new LinkedHashSet<>();

}