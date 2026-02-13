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

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Integer trangThaiHoanTien, Instant tuNgay, Instant denNgay, Pageable pageable){
        return hoaDonThanhToanRepository.searchHoaDon(key, trangThai, trangThaiHoanTien, tuNgay, denNgay, pageable);
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
        hd.setTrangThaiHoaDon(0);

        //  XỬ LÝ LOGIC HOÀN TIỀN CỌC ---
        BigDecimal tienCoc = hd.getTienCoc();
        if (tienCoc != null && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            if (Boolean.TRUE.equals(request.getIsLoiDoQuan())) {
                // Lỗi do QUÁN: Chờ hoàn tiền (1), Hoàn trả = Tiền cọc
                hd.setTrangThaiHoanTien(1);
                hd.setTienHoanTra(tienCoc);
            } else {
                // Lỗi do KHÁCH (hoặc hủy sát giờ): Không hoàn (3), Hoàn trả = 0
                hd.setTrangThaiHoanTien(3);
                hd.setTienHoanTra(BigDecimal.ZERO);
            }
        } else {
            hd.setTrangThaiHoanTien(0);
            hd.setTienHoanTra(BigDecimal.ZERO);
        }

        List<ChiTietHoaDon> listgetAllMonAn = chiTietHoaDonRepository.findByIdHoaDon(hd.getId());
        for (ChiTietHoaDon chiTietHoaDon : listgetAllMonAn){
            chiTietHoaDon.setTrangThaiMon(0);
        }
        chiTietHoaDonRepository.saveAll(listgetAllMonAn);
        hoaDonThanhToanRepository.save(hd);
        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }
        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Hủy hóa đơn");
        String prefixLyDo = "";
        if (tienCoc != null && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = Boolean.TRUE.equals(request.getIsLoiDoQuan()) ? "[Hoàn cọc] " : "[Mất cọc] ";
        }
        log.setLyDoThucHien(prefixLyDo + request.getLyDoThucHien());

        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(0);

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

        Instant now = Instant.now();
        hd.setTrangThaiHoaDon(2);
        hd.setThoiGianThanhToan(now);
        hoaDonThanhToanRepository.save(hd);

        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(3);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Thanh toán");
        log.setTrangThaiTruocDo(1);
        log.setTrangThaiMoi(2);
        log.setThoiGianThucHien(now);
        System.out.println(now);
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

        // 2. Tìm xem bàn này đang có hóa đơn nào chưa thanh toán không (Trạng thái = 1)
        Optional<HoaDonThanhToan> existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(req.getIdBanAn());

        HoaDonThanhToan hoaDon;

        if (existingBill.isPresent()) {
            // === TRƯỜNG HỢP: KHÁCH GỌI THÊM MÓN ===
            hoaDon = existingBill.get();
            if (req.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(req.getIdNhanVien()).orElse(null);
                hoaDon.setIdNhanVien(nv);
            }
        } else {
            // === TRƯỜNG HỢP: KHÁCH MỚI (TẠO HÓA ĐƠN MỚI) ===
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
            hoaDon.setTrangThaiHoaDon(2);
            hoaDon.setTongTienChuaGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

            hoaDon = hoaDonThanhToanRepository.save(hoaDon);

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
            chiTiet.setTrangThaiMon(0);
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
