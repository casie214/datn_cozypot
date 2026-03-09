package com.example.datn_cozypot_spring_boot.service.BookingService;

import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final PhieuDatBanRepository phieuDatBanRepo;
    private final ChiTietHoaDonRepository chiTietHoaDonRepo;
    private final KhachHangRepository khachHangRepo;
    private final BanAnRepository banAnRepository;
    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final ThamSoHeThongRepository thamSoHeThongRepository;

    @Transactional
    public String taoDonDatBan(PhieuDatBanRequest request) {

        KhachHang khachHang = null;
        if (request.getIdKhachHang() != null) {
            khachHang = khachHangRepo.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại!"));
        }

        BanAn banAn = null;
        if (request.getIdBanAn() != null) {
            banAn = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại!"));
        }

        // ==========================================
        // 1. Lưu Phiếu Đặt Bàn (CẬP NHẬT N-N)
        // ==========================================
        PhieuDatBan phieuDatBan = new PhieuDatBan();
        phieuDatBan.setIdKhachHang(khachHang);

        if (banAn != null) {
            // 🚨 N-N LOGIC: Nhét bàn vào Set, thay vì setIdBanAn
            phieuDatBan.getBanAns().add(banAn);

            // Cập nhật trạng thái bàn sang "Đã đặt" (2) để sơ đồ khóa lại
            banAn.setTrangThai(2);
            banAnRepository.save(banAn);
        }

        // BỎ QUA setMaDatBan vì DB đã cấu hình AS ISNULL(...) PERSISTED
        phieuDatBan.setThoiGianDat(request.getThoiGianDat());
        phieuDatBan.setSoLuongKhach(request.getSoNguoi());
        phieuDatBan.setHinhThucDat(1); // 1 = Đặt online
        phieuDatBan.setTrangThai(0);   // 0 = Chờ xác nhận
        phieuDatBan.setNgayTao(LocalDateTime.now());

        PhieuDatBan savedPhieu = phieuDatBanRepo.save(phieuDatBan);

        // ==========================================
        // 2. Lưu Hóa Đơn Thanh Toán
        // ==========================================
        HoaDonThanhToan hoaDon = new HoaDonThanhToan();
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdPhieuDatBan(savedPhieu);

        // 🚨 ĐÃ XÓA hoaDon.setIdBanAn(banAn) VÌ CỘT NÀY KHÔNG CÒN TỒN TẠI

        // BỎ QUA setMaHoaDon vì DB tự gen
        hoaDon.setTongTienChuaGiam(request.getTongTien());
        hoaDon.setTongTienThanhToan(request.getTongTien());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setThoiGianTao(Instant.now());
        hoaDon.setTrangThaiHoaDon(0); // 0 = Chưa thanh toán
        float VAT = 10.0F;
        try {
            VAT = Integer.parseInt(thamSoHeThongRepository.findByMaThamSo("VAT").get().getGiaTri());
            System.out.println(VAT);
        } catch (Exception e) {}
        hoaDon.setVatApDung(VAT);

        HoaDonThanhToan savedHoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // ==========================================
        // 3. Lưu Chi Tiết Hóa Đơn (Món Ăn)
        // ==========================================
        if (request.getChiTiet() != null && !request.getChiTiet().isEmpty()) {
            for (PhieuDatBanRequest.ChiTietMonAnRequest item : request.getChiTiet()) {
                ChiTietHoaDon chiTiet = new ChiTietHoaDon();
                chiTiet.setIdHoaDon(savedHoaDon);

                if (item.getIdChiTietMonAn() != null) {
                    DanhMucChiTiet monAn = new DanhMucChiTiet();
                    monAn.setId(item.getIdChiTietMonAn());
                    chiTiet.setIdChiTietMonAn(monAn);
                } else if (item.getIdSetLau() != null) {
                    SetLau setLau = new SetLau();
                    setLau.setId(item.getIdSetLau());
                    chiTiet.setIdSetLau(setLau);
                }

                chiTiet.setSoLuong(item.getSoLuong());
                chiTiet.setDonGiaTaiThoiDiemBan(item.getDonGia());

                BigDecimal thanhTien = item.getDonGia().multiply(new BigDecimal(item.getSoLuong()));
                chiTiet.setThanhTien(thanhTien);

                chiTiet.setTrangThaiMon(1); // 1 = Chờ làm món
                chiTiet.setNgayGioTao(LocalDateTime.now());

                chiTietHoaDonRepo.save(chiTiet);
            }
        }

        return "Đặt bàn thành công! Hóa đơn đã được khởi tạo.";
    }
}
