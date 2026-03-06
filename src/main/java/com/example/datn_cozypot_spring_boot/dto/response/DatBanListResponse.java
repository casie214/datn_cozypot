package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Data
public class DatBanListResponse {
    private Integer idDatBan;
    private String maDatBan;
    private Integer idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;

    private Integer idBanAn; // 🚨 Thêm idBanAn để FE dễ dàng matching
    private String tenBan;
    private String maBan;
    private Integer soTang;

    private LocalDateTime thoiGianDat;
    private Integer soLuongKhach;
    private Integer trangThai;

    @JsonGetter("tenTrangThai")
    public String getTenTrangThai() {
        if (this.trangThai == null) return "Không xác định";
        return switch (this.trangThai) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Đã hủy";
            case 3 -> "Đang sử dụng";
            case 4 -> "Hoàn thành";
            case 5 -> "Quá hạn";
            default -> "Không xác định";
        };
    }

    public DatBanListResponse(PhieuDatBan phieuDatBan){
        this.idDatBan = phieuDatBan.getId();
        this.maDatBan = phieuDatBan.getMaDatBan();

        if (phieuDatBan.getIdKhachHang() != null) {
            this.tenKhachHang = phieuDatBan.getIdKhachHang().getTenKhachHang();
            this.idKhachHang = phieuDatBan.getIdKhachHang().getId();
            this.soDienThoai = phieuDatBan.getIdKhachHang().getSoDienThoai();
        } else {
            this.tenKhachHang = "Khách vãng lai";
        }

        // 🚨 LOGIC N-N: Xử lý lấy thông tin Bàn từ Set<BanAn>
        if (phieuDatBan.getBanAns() != null && !phieuDatBan.getBanAns().isEmpty()) {
            // Nối tên tất cả các bàn để hiển thị UI (Ví dụ: "Bàn S01, Bàn S02")
            this.tenBan = phieuDatBan.getBanAns().stream()
                    .map(BanAn::getTenBan)
                    .collect(Collectors.joining(", "));

            // Lấy Bàn đầu tiên làm đại diện để map ID và Mã Bàn cho Frontend xử lý logic
            BanAn banDaiDien = phieuDatBan.getBanAns().stream().findFirst().orElse(null);
            if (banDaiDien != null) {
                this.idBanAn = banDaiDien.getId();
                this.maBan = banDaiDien.getMaBan();
                this.soTang = (banDaiDien.getIdKhuVuc() != null) ? banDaiDien.getIdKhuVuc().getTang() : null;
            }
        }

        this.thoiGianDat = phieuDatBan.getThoiGianDat();
        this.soLuongKhach = phieuDatBan.getSoLuongKhach();
        this.trangThai = phieuDatBan.getTrangThai();
    }
}