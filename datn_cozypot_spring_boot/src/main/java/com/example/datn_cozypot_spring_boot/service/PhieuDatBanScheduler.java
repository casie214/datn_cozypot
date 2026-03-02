package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
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
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

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

                List<ChiTietHoaDon> listMonAn = chiTietHoaDonRepository.findByIdHoaDon(hoaDon.getId());
                if (listMonAn != null && !listMonAn.isEmpty()) {
                    for (ChiTietHoaDon chiTiet : listMonAn) {
                        chiTiet.setTrangThaiMon(0);
                    }
                    chiTietHoaDonRepository.saveAll(listMonAn);
                }

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

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelUnpaidDeposits() {
        // Mốc thời gian: Hiện tại trừ đi 15 phút (Tính từ lúc tạo đơn)
        Instant limitTime = Instant.now().minus(15, java.time.temporal.ChronoUnit.MINUTES);

        // Tìm các hóa đơn đang ở trạng thái 1 (Chờ cọc) và đã quá 15 phút
        List<HoaDonThanhToan> expiredDeposits = hoaDonThanhToanRepository.findAllByTrangThaiHoaDonAndThoiGianTaoBefore(1, limitTime);

        for (HoaDonThanhToan hoaDon : expiredDeposits) {
            System.out.println("Hệ thống tự động hủy đơn do không cọc: Hóa đơn ID " + hoaDon.getId());

            Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

            hoaDon.setTrangThaiHoaDon(8);

            PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
            if (phieu != null) {
                phieu.setTrangThai(2);
                phieuDatBanRepository.save(phieu);
            }

            if (hoaDon.getIdBanAn() != null) {
                BanAn ban = hoaDon.getIdBanAn();
                ban.setTrangThai(0);
                banAnRepository.save(ban);
            }

            List<ChiTietHoaDon> listMonAn = chiTietHoaDonRepository.findByIdHoaDon(hoaDon.getId());
            if (listMonAn != null && !listMonAn.isEmpty()) {
                for (ChiTietHoaDon chiTiet : listMonAn) {
                    chiTiet.setTrangThaiMon(0);
                }
                chiTietHoaDonRepository.saveAll(listMonAn);
            }

            hoaDonThanhToanRepository.save(hoaDon);

            ghiLichSu(
                    hoaDon,
                    null,
                    "Hủy tự động (Quá hạn cọc)",
                    "Khách không thanh toán cọc trong vòng 15 phút kể từ lúc đặt",
                    trangThaiCu,
                    8
            );
        }
    }
}
