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
    @JoinColumn(name = "id_chi_tiet_mon_an", nullable = false)
    private ChiTietMonAn chiTietMonAn;

    @Column(name = "so_luong")
    private Integer soLuong;
}
