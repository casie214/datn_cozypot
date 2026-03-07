package com.example.datn_cozypot_spring_boot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PhieuDatBanBanAnId implements Serializable {
    @Column(name = "id_phieu_dat_ban")
    private Integer idPhieuDatBan;

    @Column(name = "id_ban_an")
    private Integer idBanAn;
}

