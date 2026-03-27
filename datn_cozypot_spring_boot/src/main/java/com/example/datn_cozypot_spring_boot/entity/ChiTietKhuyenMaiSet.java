package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_khuyen_mai_set")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKhuyenMaiSet {

    @EmbeddedId
    private ChiTietKhuyenMaiSetId id;

    @ManyToOne
    @MapsId("idDotKhuyenMai") // Ánh xạ vào trường trong class Id ở trên
    @JoinColumn(name = "id_dot_khuyen_mai")
    private DotKhuyenMai dotKhuyenMai;

    @ManyToOne
    @MapsId("idSetLau")
    @JoinColumn(name = "id_set_lau")
    private SetLau setLau;
}