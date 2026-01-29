package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class BanAnResponse {
    private Integer id;
    private String maBan;
    private Integer soCho;
    private Integer soTang;    // Bổ sung để Vue lọc theo tầng
    private Integer trangThai; // Lưu mã số (0, 1, 2) để logic so sánh nhanh hơn
    private Integer loaiDatBan; // 0: chỉ đặt tại quầy; 1: cho đặt online
    private Integer idKhuVuc;
    private String tenKhuVuc;
    private Instant ngayTao;
    private String nguoiTao;


    // ✅ Tự động tạo trường "tenTrangThai" trong JSON trả về
    @JsonGetter("tenTrangThai")
    public String getTenTrangThai() {
        if (this.trangThai == null) return "Không xác định";
        return switch (this.trangThai) {
            case 0 -> "Trống";
            case 1 -> "Có khách";
            case 2 -> "Đã đặt";
            case 3 -> "Đang dọn";
            default -> "Không xác định";
        };
    }

    public BanAnResponse(BanAn ban) {

        this.id = ban.getId();
        this.maBan = ban.getMaBan();
        this.soCho = ban.getSoNguoiToiDa();
        this.soTang = ban.getIdKhuVuc() != null
                ? ban.getIdKhuVuc().getTang()
                : null;

        this.loaiDatBan = ban.getLoaiDatBan();
        this.idKhuVuc = ban.getIdKhuVuc().getId();
        this.tenKhuVuc = ban.getIdKhuVuc().getTenKhuVuc();
        this.ngayTao = ban.getNgayTao();
        this.nguoiTao = ban.getNguoiTao();

        // ⭐ LOGIC CHUẨN: kiểm tra phiếu đặt hợp lệ
        boolean hasValidBooking = ban.getPhieuDatBans().stream()
                .anyMatch(this::isValidBooking);

        if (hasValidBooking) {
            this.trangThai = 2; // Đã đặt
        } else {
            this.trangThai = ban.getTrangThai(); // 0 | 1
        }
    }

    private boolean isValidBooking(PhieuDatBan p) {
        return p.getTrangThai() == 1 // đang đặt
                && p.getThoiGianDat() != null
                && p.getThoiGianDat().isAfter(Instant.now());
    }
}