package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chi_tiet_set_lau")
public class ChiTietSetLau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_set_lau", nullable = false)
    @JsonIgnore
    private SetLau setLau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_danh_muc_chi_tiet")
    private DanhMucChiTiet monAn;

    @Column(name = "so_luong")
    private Integer soLuong;
}
