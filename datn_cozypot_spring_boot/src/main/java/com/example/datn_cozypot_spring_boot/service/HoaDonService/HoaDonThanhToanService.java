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
import java.util.*;
import java.util.stream.Collectors;

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
    PhieuDatBanRepository phieuDatBanRepository;

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
            lsThanhToan.setHoaDon(hd);
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

        if (hd.getIdPhieuDatBan() != null) {
            PhieuDatBan phieu = hd.getIdPhieuDatBan();
            phieu.setTrangThai(2);
            phieuDatBanRepository.save(phieu);
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
        lsThanhToan.setHoaDon(hd);
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
        // 1. TÌM CHÍNH XÁC HÓA ĐƠN ĐANG THAO TÁC
        BanAn banAn = banAnRepo.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        Optional<HoaDonThanhToan> existingBill;
        if (req.getIdHoaDon() != null) {
            existingBill = hoaDonThanhToanRepository.findById(req.getIdHoaDon());
        } else {
            existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(req.getIdBanAn());
        }

        HoaDonThanhToan hoaDon;

        if (existingBill.isPresent()) {
            hoaDon = existingBill.get();
        } else {
            hoaDon = new HoaDonThanhToan();
            hoaDon.setIdBanAn(banAn);
            // ... set các thông tin cơ bản cho hóa đơn mới như cũ
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

        // =======================================================
        // 2. ĐỒNG BỘ CHI TIẾT MÓN ĂN
        // =======================================================
        List<ChiTietHoaDon> existingDetails = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
        if (existingDetails == null) existingDetails = new ArrayList<>();

        List<ChiTietHoaDon> itemsToKeep = new ArrayList<>();
        BigDecimal tongTienMoi = BigDecimal.ZERO;

        List<ChiTietHoaDonRequest> requestDetails = req.getChiTietHoaDon();
        if (requestDetails == null) requestDetails = new ArrayList<>();

        for (ChiTietHoaDonRequest itemReq : requestDetails) {

            // ÉP KIỂU VỀ STRING ĐỂ SO SÁNH (CHỐNG LỖI INTEGER KHÔNG BẰNG LONG)
            Optional<ChiTietHoaDon> matchOpt = existingDetails.stream().filter(dbItem -> {
                boolean isFoodMatch = itemReq.getIdChiTietMonAn() != null && dbItem.getIdChiTietMonAn() != null
                        && String.valueOf(itemReq.getIdChiTietMonAn()).equals(String.valueOf(dbItem.getIdChiTietMonAn().getId()));

                boolean isSetMatch = itemReq.getIdSetLau() != null && dbItem.getIdSetLau() != null
                        && String.valueOf(itemReq.getIdSetLau()).equals(String.valueOf(dbItem.getIdSetLau().getId()));

                return isFoodMatch || isSetMatch;
            }).findFirst();

            ChiTietHoaDon chiTiet;
            BigDecimal donGiaGoc;

            if (matchOpt.isPresent()) {
                // ĐÃ TỒN TẠI -> Cập nhật số lượng
                chiTiet = matchOpt.get();
                donGiaGoc = chiTiet.getDonGiaTaiThoiDiemBan();
                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());
            } else {
                // MỚI TOANH -> Tạo mới
                chiTiet = new ChiTietHoaDon();
                chiTiet.setIdHoaDon(hoaDon);

                if (itemReq.getIdChiTietMonAn() != null) {
                    DanhMucChiTiet monAn = chiTietDanhMucChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                            .orElseThrow(() -> new RuntimeException("Lỗi ID Món"));
                    chiTiet.setIdChiTietMonAn(monAn);
                    donGiaGoc = monAn.getGiaBan();
                } else if (itemReq.getIdSetLau() != null) {
                    SetLau setLau = setLauRepository.findById(itemReq.getIdSetLau())
                            .orElseThrow(() -> new RuntimeException("Lỗi ID Set"));
                    chiTiet.setIdSetLau(setLau);
                    donGiaGoc = setLau.getGiaBan();
                } else {
                    continue;
                }

                chiTiet.setDonGiaTaiThoiDiemBan(donGiaGoc);
                chiTiet.setTrangThaiMon(1);
                chiTiet.setNgayGioTao(LocalDateTime.now());
                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());
            }

            BigDecimal thanhTien = donGiaGoc.multiply(BigDecimal.valueOf(itemReq.getSoLuong()));
            chiTiet.setThanhTien(thanhTien);

            chiTiet = chiTietHoaDonRepository.save(chiTiet);
            itemsToKeep.add(chiTiet);
            tongTienMoi = tongTienMoi.add(thanhTien);
        }

        // =======================================================
        // 3. TÌM VÀ DIỆT CÁC MÓN KHÁCH ĐÃ XÓA
        // =======================================================
        List<ChiTietHoaDon> toDelete = existingDetails.stream()
                .filter(oldItem -> !itemsToKeep.contains(oldItem))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            // Cắt đứt quan hệ với Entity Hóa đơn để Hibernate cho phép xóa
            if (hoaDon.getChiTietHoaDons() != null) {
                hoaDon.getChiTietHoaDons().removeAll(toDelete);
            }

            // Xóa sổ khỏi DB
            chiTietHoaDonRepository.deleteAll(toDelete);
            chiTietHoaDonRepository.flush(); // Bắt buộc thực thi ngay lập tức
        }

        // 4. CHỐT TỔNG TIỀN VÀ LƯU
        hoaDon.setTongTienChuaGiam(tongTienMoi);
        BigDecimal giamGia = hoaDon.getSoTienDaGiam() == null ? BigDecimal.ZERO : hoaDon.getSoTienDaGiam();
        hoaDon.setTongTienThanhToan(tongTienMoi.subtract(giamGia));

        hoaDonThanhToanRepository.save(hoaDon);
    }
}
