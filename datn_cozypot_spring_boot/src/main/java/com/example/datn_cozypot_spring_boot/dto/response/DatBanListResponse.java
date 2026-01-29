package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.fasterxml.jackson.annotation.JsonGetter; // Thêm import này
import lombok.Data;
import java.time.Instant;

@Data
public class DatBanListResponse {
    private Integer idDatBan;
    private String maDatBan;
    private String tenKhachHang;
    private String soDienThoai;
    private String tenBan;
    private String maBan;
    private Integer soTang;
    private Instant thoiGianDat;
    private Integer soLuongKhach;
    private Integer trangThai;

    // ✅ Bổ sung phương thức này để Vue nhận được chuỗi tiếng Việt
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
        this.tenKhachHang = phieuDatBan.getIdKhachHang().getTenKhachHang();
        this.soDienThoai = phieuDatBan.getIdKhachHang().getSoDienThoai();
        this.tenBan = phieuDatBan.getIdBanAn() != null
                ? phieuDatBan.getIdBanAn().getTenBan()
                : null;
        this.maBan = (phieuDatBan.getIdBanAn() != null) ? phieuDatBan.getIdBanAn().getMaBan() : null;
        this.soTang = phieuDatBan.getIdBanAn() != null
                ? phieuDatBan.getIdBanAn().getIdKhuVuc().getTang()
                : null;
        this.thoiGianDat = phieuDatBan.getThoiGianDat();
        this.soLuongKhach = phieuDatBan.getSoLuongKhach();
        this.trangThai = phieuDatBan.getTrangThai();
    }
}