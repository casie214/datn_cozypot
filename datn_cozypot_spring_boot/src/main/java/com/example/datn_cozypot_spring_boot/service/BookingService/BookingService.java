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
    private final HoaDonThanhToanRepository hoaDonRepo;
    private final ChiTietHoaDonRepository chiTietHoaDonRepo;
    private final KhachHangRepository khachHangRepo;
    private final BanAnRepository banAnRepository;
    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;

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

        // 1. Lưu Phiếu Đặt Bàn
        PhieuDatBan phieuDatBan = new PhieuDatBan();
        phieuDatBan.setIdKhachHang(khachHang);
        phieuDatBan.setIdBanAn(banAn);

        // Sử dụng một cơ chế sinh mã duy nhất (ví dụ: PDB + timestamp)
        // Lưu ý: Đảm bảo độ dài mã không vượt quá cấu trúc của bảng (varchar 30 hoặc 50)
        String maPhieu = "PDB" + System.currentTimeMillis();
        phieuDatBan.setMaDatBan(maPhieu.length() > 30 ? maPhieu.substring(0, 30) : maPhieu);

        phieuDatBan.setThoiGianDat(request.getThoiGianDat());
        phieuDatBan.setSoLuongKhach(request.getSoNguoi());
        phieuDatBan.setHinhThucDat(1); // Ví dụ: 1 = Đặt online
        phieuDatBan.setTrangThai(1);   // Ví dụ: 0 = Chờ check-in
        phieuDatBan.setNgayTao(LocalDateTime.now());


        // Cần lưu ý: Nếu Database đã set "ma_dat_ban" là Computed Column hoặc Trigger,
        // bạn có thể cần bỏ qua việc set trường này hoặc điều chỉnh cấu trúc bảng.

        PhieuDatBan savedPhieu = phieuDatBanRepo.save(phieuDatBan);

        // 2. Lưu Hóa Đơn Thanh Toán
        HoaDonThanhToan hoaDon = new HoaDonThanhToan();
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdBanAn(banAn);
        hoaDon.setIdPhieuDatBan(savedPhieu);

        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon.length() > 30 ? maHoaDon.substring(0, 30) : maHoaDon);

        hoaDon.setTongTienChuaGiam(request.getTongTien());
        hoaDon.setTongTienThanhToan(request.getTongTien());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setThoiGianTao(Instant.now());
        hoaDon.setTrangThaiHoaDon(0); // Ví dụ: 0 = Chưa thanh toán

        HoaDonThanhToan savedHoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // 3. Lưu Chi Tiết Hóa Đơn
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

                chiTiet.setTrangThaiMon(0); // Ví dụ: 0 = Chờ làm món
                chiTiet.setNgayGioTao(LocalDateTime.now());

                chiTietHoaDonRepo.save(chiTiet);
            }
        }

        return "Đặt bàn thành công với mã: " + savedPhieu.getMaDatBan();
    }
}
