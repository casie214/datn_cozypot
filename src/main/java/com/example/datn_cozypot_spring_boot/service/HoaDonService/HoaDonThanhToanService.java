package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonThanhToanService {
    @Autowired
    HoaDonThanhToanRepository hoaDonThanhToanRepository;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @Autowired
    ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    BanAnRepository banAnRepo;

    @Autowired
    DanhMucChiTietRepository chiTietDanhMucChiTietRepository;
    @Autowired
    private SetLauRepository setLauRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;

    public Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable){
        return hoaDonThanhToanRepository.getAllHoaDon(pageable);
    }

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Instant tuNgay, Instant denNgay, Pageable pageable){
        return hoaDonThanhToanRepository.searchHoaDon(key, trangThai, tuNgay, denNgay, pageable);
    }

    public HoaDonThanhToanResponse getHoaDonById(Integer id) {
        return hoaDonThanhToanRepository.getHoaDonById(id);
    }

    @Transactional
    public void huyHoaDon(LichSuHoaDonRequest request) {
        if (chiTietHoaDonService.hasAnyDishServed(request.getIdHoaDon())) {
            throw new RuntimeException("Không thể hủy hóa đơn vì đã có món ăn được phục vụ!");
        }

        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu == 7 || trangThaiHDCu == 8 || trangThaiHDCu == 9) {
            throw new RuntimeException("Hóa đơn đã ở trạng thái kết thúc, không thể hủy!");
        }

        BigDecimal tienCoc = hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO;
        int trangThaiMoi;

        if (tienCoc.compareTo(BigDecimal.ZERO) > 0 && Boolean.TRUE.equals(request.getIsLoiDoQuan())) {
            // TRƯỜNG HỢP 1: Có cọc và Lỗi do quán -> Hoàn tiền
            hd.setTienHoanTra(tienCoc);
            trangThaiMoi = 9;
            hd.setTrangThaiHoaDon(trangThaiMoi);

            LichSuThanhToan lsThanhToan = new LichSuThanhToan();
            lsThanhToan.setIdHoaDon(hd);
            lsThanhToan.setSoTienThanhToan(tienCoc);
            lsThanhToan.setLoaiGiaoDich(3);
            lsThanhToan.setGhiChu("Hoàn trả cọc do hủy hóa đơn: " + request.getLyDoThucHien());
            lsThanhToan.setNgayThanhToan(Instant.now());
            lsThanhToan.setTrangThai(1);
            lsThanhToan.setTenPhuongThuc("Chuyển khoản");

            // TẠO MÃ GIAO DỊCH FAKE GIỐNG THẬT
            // Ví dụ kết quả: REFUND_HD00015_1708945672341
            String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
            String fakeMaGiaoDich = "REFUND_" + maHoaDonStr + "_" + System.currentTimeMillis();
            lsThanhToan.setMaGiaoDich(fakeMaGiaoDich);

            lichSuThanhToanRepository.save(lsThanhToan);

        } else {
            // TRƯỜNG HỢP 2: Không cọc hoặc Lỗi do khách -> Hủy luôn, mất cọc
            hd.setTienHoanTra(BigDecimal.ZERO);
            trangThaiMoi = 8;
            hd.setTrangThaiHoaDon(trangThaiMoi);
        }

        // === CẬP NHẬT CÁC BẢNG LIÊN QUAN ===
        List<ChiTietHoaDon> listgetAllMonAn = chiTietHoaDonRepository.findByIdHoaDon(hd.getId());
        for (ChiTietHoaDon chiTietHoaDon : listgetAllMonAn){
            chiTietHoaDon.setTrangThaiMon(0);
        }
        chiTietHoaDonRepository.saveAll(listgetAllMonAn);

        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }

        hoaDonThanhToanRepository.save(hd);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(trangThaiMoi == 9 ? "Hủy & Hoàn tiền" : "Hủy hóa đơn");

        String prefixLyDo = "";
        if (tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = Boolean.TRUE.equals(request.getIsLoiDoQuan()) ? "[Đã hoàn cọc] " : "[Mất cọc] ";
        }
        log.setLyDoThucHien(prefixLyDo + request.getLyDoThucHien());

        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(trangThaiMoi);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void thanhToanHoaDon(LichSuHoaDonRequest request) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu == 6 || trangThaiHDCu == 7 || trangThaiHDCu == 8 || trangThaiHDCu == 9) {
            throw new RuntimeException("Hóa đơn đã được thanh toán hoặc đã kết thúc!");
        }

        Instant now = Instant.now();

        hd.setTrangThaiHoaDon(6);
        hd.setThoiGianThanhToan(now);
        hoaDonThanhToanRepository.save(hd);

        if (hd.getIdBanAn() != null){
            hd.getIdBanAn().setTrangThai(3);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuThanhToan lsThanhToan = new LichSuThanhToan();
        lsThanhToan.setIdHoaDon(hd);
        lsThanhToan.setSoTienThanhToan(hd.getTongTienThanhToan());
        lsThanhToan.setLoaiGiaoDich(1);
        lsThanhToan.setGhiChu("Khách thanh toán tại quầy");
        lsThanhToan.setNgayThanhToan(now);
        lsThanhToan.setTrangThai(1);
        lsThanhToan.setTenPhuongThuc("Tiền mặt");
        lsThanhToan.setMaGiaoDich("CASH_" + hd.getMaHoaDon() + "_" + System.currentTimeMillis());
        lichSuThanhToanRepository.save(lsThanhToan);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Thanh toán hóa đơn");
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(6);
        log.setThoiGianThucHien(now);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void hoanTatHoaDon(LichSuHoaDonRequest request) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu != 6) {
            throw new RuntimeException("Hóa đơn chưa được thanh toán, không thể hoàn tất!");
        }

        Instant now = Instant.now();

        hd.setTrangThaiHoaDon(7);
        hoaDonThanhToanRepository.save(hd);

        if (hd.getIdBanAn() != null) {
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Khách ra về - Hoàn tất hóa đơn");
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(7);
        log.setThoiGianThucHien(now);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void createOrder(HoaDonThanhToanRequest req) {
        // 1. Validate Bàn ăn
        BanAn banAn = banAnRepo.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại với ID: " + req.getIdBanAn()));

        // 2. Tìm xem bàn này đang có hóa đơn nào chưa thanh toán không (Trạng thái 4 hoặc 5)
        Optional<HoaDonThanhToan> existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(req.getIdBanAn());

        HoaDonThanhToan hoaDon;

        if (existingBill.isPresent()) {
            // === TRƯỜNG HỢP: KHÁCH GỌI THÊM MÓN ===
            hoaDon = existingBill.get();
            LichSuHoaDon logGoiMon = new LichSuHoaDon();
            logGoiMon.setIdHoaDon(hoaDon);
            logGoiMon.setHanhDong("Gọi thêm món");
            logGoiMon.setTrangThaiTruocDo(4);
            logGoiMon.setTrangThaiMoi(4);
            logGoiMon.setThoiGianThucHien(Instant.now());
            if (req.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(req.getIdNhanVien()).orElse(null);
                logGoiMon.setIdNhanVien(nv);
            }
            lichSuHoaDonRepository.save(logGoiMon);

        } else {
            // === TRƯỜNG HỢP: KHÁCH MỚI (TẠO HÓA ĐƠN MỚI / MỞ BÀN) ===
            hoaDon = new HoaDonThanhToan();
            hoaDon.setIdBanAn(banAn);

            if (req.getIdNhanVien() != null) {
                NhanVien nhanVien = nhanVienRepository.findById(req.getIdNhanVien())
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
                hoaDon.setIdNhanVien(nhanVien);
            }

            if (req.getIdKhachHang() != null) {
                KhachHang khachHang = khachHangRepository.findById(req.getIdKhachHang())
                        .orElse(null);
                hoaDon.setIdKhachHang(khachHang);
            }

            hoaDon.setThoiGianTao(Instant.now());
            hoaDon.setTrangThaiHoaDon(4);
            hoaDon.setTongTienChuaGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

            hoaDon = hoaDonThanhToanRepository.save(hoaDon);

            LichSuHoaDon logTaoMoi = new LichSuHoaDon();
            logTaoMoi.setIdHoaDon(hoaDon);
            logTaoMoi.setHanhDong("Mở bàn");
            logTaoMoi.setTrangThaiMoi(4);
            logTaoMoi.setThoiGianThucHien(Instant.now());
            logTaoMoi.setIdNhanVien(hoaDon.getIdNhanVien());
            lichSuHoaDonRepository.save(logTaoMoi);

            banAn.setTrangThai(1);
            banAnRepo.save(banAn);
        }

        BigDecimal tongTienThem = BigDecimal.ZERO;

        for (ChiTietHoaDonRequest item : req.getChiTietHoaDon()) {
            ChiTietHoaDon chiTiet = new ChiTietHoaDon();
            chiTiet.setIdHoaDon(hoaDon);

            BigDecimal donGiaGoc = BigDecimal.ZERO;

            if (item.getIdChiTietMonAn() != null) {
                DanhMucChiTiet monAn = chiTietDanhMucChiTietRepository.findById(item.getIdChiTietMonAn())
                        .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại ID: " + item.getIdChiTietMonAn()));

                chiTiet.setIdChiTietMonAn(monAn);
                donGiaGoc = monAn.getGiaBan();
            }

            else if (item.getIdSetLau() != null) {
                SetLau setLau = setLauRepository.findById(item.getIdSetLau())
                        .orElseThrow(() -> new RuntimeException("Set lẩu không tồn tại ID: " + item.getIdSetLau()));

                chiTiet.setIdSetLau(setLau);
                donGiaGoc = setLau.getGiaBan();
            } else {
                throw new RuntimeException("Dữ liệu món ăn không hợp lệ (Phải có ID món hoặc ID Set)");
            }

            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setDonGiaTaiThoiDiemBan(donGiaGoc);

            BigDecimal thanhTien = donGiaGoc.multiply(BigDecimal.valueOf(item.getSoLuong()));
            chiTiet.setThanhTien(thanhTien);

            chiTiet.setGhiChuMon(item.getGhiChu());
            chiTiet.setTrangThaiMon(1);
            chiTiet.setNgayGioTao(LocalDateTime.now());

            chiTietHoaDonRepository.save(chiTiet);

            tongTienThem = tongTienThem.add(thanhTien);
        }

        BigDecimal tongCu = hoaDon.getTongTienChuaGiam() == null ? BigDecimal.ZERO : hoaDon.getTongTienChuaGiam();
        BigDecimal tongMoi = tongCu.add(tongTienThem);

        hoaDon.setTongTienChuaGiam(tongMoi);

        BigDecimal giamGia = hoaDon.getSoTienDaGiam() == null ? BigDecimal.ZERO : hoaDon.getSoTienDaGiam();
        hoaDon.setTongTienThanhToan(tongMoi.subtract(giamGia));

        hoaDonThanhToanRepository.save(hoaDon);
    }

}
