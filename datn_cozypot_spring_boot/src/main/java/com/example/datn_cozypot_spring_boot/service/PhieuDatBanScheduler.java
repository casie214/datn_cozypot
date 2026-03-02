package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.entity.LichSuHoaDon;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.LichSuHoaDonRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PhieuDatBanScheduler {

    private final PhieuDatBanRepository phieuDatBanRepository;
    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final BanAnRepository banAnRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void autoCancelExpiredReservations() {
        // Mốc thời gian: Hiện tại trừ đi 15 phút
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(15);

        // Tìm các phiếu ở trạng thái 2 (Đã xác nhận/Chờ) và thoiGianDat < limitTime
        List<PhieuDatBan> expiredReservations = phieuDatBanRepository.findAllByTrangThaiAndThoiGianDatBefore(1, limitTime);

        for (PhieuDatBan phieu : expiredReservations) {
            System.out.println("🤖 Hệ thống tự động hủy phiếu quá hạn: " + phieu.getMaDatBan());

            // 1. Hủy Phiếu Đặt Bàn (Trạng thái = 0)
            phieu.setTrangThai(5);
            phieuDatBanRepository.save(phieu);

            // 2. Tìm Hóa Đơn liên quan đến phiếu này
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieu.getId());
            if (hoaDon != null) {
                Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

                // Hủy Hóa Đơn (Trạng thái = 8)
                hoaDon.setTrangThaiHoaDon(8);
                hoaDonThanhToanRepository.save(hoaDon);

                // 📝 3. Ghi log vào Timeline Lịch sử hóa đơn
                ghiLichSu(
                        hoaDon,
                        null, // Nhân viên là null vì hệ thống tự thực hiện
                        "Tự động hủy hóa đơn",
                        "Quá 15 phút kể từ giờ hẹn mà khách không check-in",
                        trangThaiCu,
                        8
                );
            }

            // 4. Giải phóng Bàn Ăn (Trạng thái = 0 - Trống)
            if (phieu.getIdBanAn() != null) {
                BanAn ban = phieu.getIdBanAn();
                ban.setTrangThai(0);
                banAnRepository.save(ban);
            }
        }
    }

    private void ghiLichSu(HoaDonThanhToan hd, Integer idNV, String hanhDong, String lyDo, Integer cu, Integer moi) {
        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(hanhDong);
        log.setLyDoThucHien(lyDo);
        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(cu);
        log.setTrangThaiMoi(moi);
        lichSuHoaDonRepository.save(log);
    }
}
