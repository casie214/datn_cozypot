package com.example.datn_cozypot_spring_boot.dto.response;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanAnResponse {
    private Integer id;
    private String maBan;
    private Integer soCho;
    private Integer soTang;    // Bổ sung để Vue lọc theo tầng
    private Integer trangThai; // Lưu mã số (0, 1, 2) để logic so sánh nhanh hơn
    private Integer loaiDatBan; // 0: chỉ đặt tại quầy; 1: cho đặt online
    private Integer idKhuVuc;
    private String tenKhuVuc;
    private LocalDateTime ngayTao;
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
        boolean hasValidBooking = ban.getPhieuDatBanBanAns().stream()
                .map(link -> link.getPhieuDatBan())
                .filter(p -> p != null)
                .anyMatch(this::isValidBooking);

        if (hasValidBooking) {
            this.trangThai = 2; // Đã đặt
        } else {
            this.trangThai = ban.getTrangThai(); // 0 | 1
        }
    }

    private boolean isValidBooking(PhieuDatBan p) {
        // 1. Kiểm tra trạng thái phiếu (Ví dụ: 1 hoặc 2 tùy theo logic của bạn, ở đây tôi giữ 1 theo code cũ của bạn)
        // Chú ý: Nếu trạng thái "Đã xác nhận" của bạn là 2 thì hãy đổi số 1 thành số 2
        if (p.getTrangThai() == null || p.getTrangThai() != 1 || p.getThoiGianDat() == null) return false;

        ZonedDateTime nowVN = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime bookingTimeVN = p.getThoiGianDat().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        // 2. Kiểm tra xem có cùng ngày hôm nay không
        boolean isToday = bookingTimeVN.toLocalDate().equals(nowVN.toLocalDate());

        // 3. Logic: Chỉ hiện "Đã đặt" nếu là phiếu của HÔM NAY
        // và thời gian khách đến phải SAU hiện tại (chưa quá giờ)
        // Bạn có thể thêm điều kiện: bookingTimeVN.isBefore(nowVN.plusHours(3)) nếu chỉ muốn khóa bàn trước 3 tiếng
        return isToday && bookingTimeVN.isAfter(nowVN);
    }
}
