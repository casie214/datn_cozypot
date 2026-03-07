package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phieu_dat_ban_ban_an")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhieuDatBanBanAn {
    @EmbeddedId
    private PhieuDatBanBanAnId id = new PhieuDatBanBanAnId();

    @MapsId("idPhieuDatBan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_dat_ban")
    private PhieuDatBan phieuDatBan;

    @MapsId("idBanAn")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ban_an")
    private BanAn banAn;
}

