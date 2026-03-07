package com.example.datn_cozypot_spring_boot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phieu_dat_ban")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhieuDatBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phieu_dat_ban", nullable = false)
    private Integer id;

    @OneToMany(mappedBy = "phieuDatBan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PhieuDatBanBanAn> dsBanAn = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @Size(max = 30)
    @Column(name = "ma_dat_ban", insertable = false, updatable = false, length = 30)
    private String maDatBan;

    @Column(name = "thoi_gian_dat")
    private LocalDateTime thoiGianDat;

    @Column(name = "hinh_thuc_dat")
    private Integer hinhThucDat;

    @Column(name = "so_luong_khach")
    private Integer soLuongKhach;

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

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idPhieuDatBan")
    private Set<HoaDonThanhToan> hoaDonThanhToans = new LinkedHashSet<>();

    @Transient
    public BanAn getIdBanAn() {
        return dsBanAn.stream()
                .map(PhieuDatBanBanAn::getBanAn)
                .findFirst()
                .orElse(null);
    }

    public void setIdBanAn(BanAn banAn) {
        dsBanAn.clear();
        if (banAn == null) return;

        PhieuDatBanBanAn link = new PhieuDatBanBanAn();
        link.setPhieuDatBan(this);
        link.setBanAn(banAn);

        dsBanAn.add(link);
    }

}
