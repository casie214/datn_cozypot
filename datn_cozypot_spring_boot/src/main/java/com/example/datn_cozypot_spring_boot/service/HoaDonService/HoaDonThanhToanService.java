package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.GopBanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    public Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable){
        return hoaDonThanhToanRepository.getAllHoaDon(pageable);
    }

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Instant tuNgayTao, Instant denNgayTao, LocalDateTime tuNgayDat, LocalDateTime denNgayDat,Pageable pageable){
        return hoaDonThanhToanRepository.searchHoaDon(key, trangThai, tuNgayTao, denNgayTao, tuNgayDat, denNgayDat, pageable);
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

        if (trangThaiHDCu >= 2 && tienCoc.compareTo(BigDecimal.ZERO) > 0 && Boolean.TRUE.equals(request.getIsLoiDoQuan())) {
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
            String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
            lsThanhToan.setMaGiaoDich("REFUND_" + maHoaDonStr + "_" + System.currentTimeMillis());
            lichSuThanhToanRepository.save(lsThanhToan);
        } else {
            hd.setTienHoanTra(BigDecimal.ZERO);
            trangThaiMoi = 8;
            hd.setTrangThaiHoaDon(trangThaiMoi);
        }

        List<ChiTietHoaDon> listgetAllMonAn = chiTietHoaDonRepository.findByIdHoaDon(hd.getId());
        for (ChiTietHoaDon chiTietHoaDon : listgetAllMonAn){
            chiTietHoaDon.setTrangThaiMon(0);
        }
        chiTietHoaDonRepository.saveAll(listgetAllMonAn);

        // 🚨 N-N LOGIC: Hủy phiếu và dọn toàn bộ danh sách bàn thuộc phiếu đó
        if (hd.getIdPhieuDatBan() != null) {
            PhieuDatBan phieu = hd.getIdPhieuDatBan();
            phieu.setTrangThai(2); // Hủy phiếu

            for (BanAn ban : phieu.getBanAns()) {
                ban.setTrangThai(0); // Bàn về trạng thái Trống
                banAnRepo.save(ban);
            }
            phieuDatBanRepository.save(phieu);
        }

        hoaDonThanhToanRepository.save(hd);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(trangThaiMoi == 9 ? "Hủy & Hoàn tiền" : "Hủy hóa đơn");
        String prefixLyDo = "";
        if (trangThaiHDCu >= 2 && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = Boolean.TRUE.equals(request.getIsLoiDoQuan()) ? "[Đã hoàn cọc] " : "[Mất cọc] ";
        } else if (trangThaiHDCu == 1) {
            prefixLyDo = "[Hủy chưa cọc] ";
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
        hd.setThoiGianThanhToan(now.plus(7, ChronoUnit.HOURS));
        hoaDonThanhToanRepository.save(hd);

        // 🚨 N-N LOGIC: Thay đổi trạng thái tất cả các bàn trong đoàn
        if (hd.getIdPhieuDatBan() != null) {
            for (BanAn ban : hd.getIdPhieuDatBan().getBanAns()) {
                ban.setTrangThai(3); // Đang dọn dẹp
                banAnRepo.save(ban);
            }
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

        hd.setTrangThaiHoaDon(7);
        hoaDonThanhToanRepository.save(hd);

        // 🚨 N-N LOGIC: Trả tất cả các bàn trong đoàn về trạng thái trống (0)
        if (hd.getIdPhieuDatBan() != null) {
            PhieuDatBan phieu = hd.getIdPhieuDatBan();
            phieu.setTrangThai(4); // Phiếu hoàn tất

            for (BanAn ban : phieu.getBanAns()) {
                ban.setTrangThai(0);
                banAnRepo.save(ban);
            }
            phieuDatBanRepository.save(phieu);
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Khách ra về - Hoàn tất hóa đơn");
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(7);
        log.setThoiGianThucHien(Instant.now());

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }
        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void createOrder(HoaDonThanhToanRequest req) {
        BanAn banAn = banAnRepo.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        HoaDonThanhToan hoaDon = null;

        // =======================================================
        // 1. TÌM HOẶC TẠO MỚI PHIẾU ĐẶT BÀN & HÓA ĐƠN (Chuẩn N-N)
        // =======================================================
        if (req.getIdHoaDon() != null) {
            hoaDon = hoaDonThanhToanRepository.findById(req.getIdHoaDon()).orElse(null);
        } else {
            // Khách vãng lai: Tìm Phiếu đang hoạt động của bàn này
            List<PhieuDatBan> listPhieu = phieuDatBanRepository.findByDsBanAn_BanAn_IdAndTrangThaiInOrderByThoiGianDatAsc(req.getIdBanAn(), Arrays.asList(0, 1, 3));
            if (!listPhieu.isEmpty()) {
                PhieuDatBan phieuActive = listPhieu.get(0);
                hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieuActive.getId());
            }
        }

        // Nếu khách tự vào bàn (chưa có phiếu nào cả) -> Tự động sinh Phiếu và Hóa đơn
        if (hoaDon == null) {
            // A. Tạo Phiếu Đặt Bàn Khách Vãng Lai
            PhieuDatBan newPhieu = new PhieuDatBan();
            newPhieu.getBanAns().add(banAn);
            newPhieu.setTrangThai(3); // 3 = Đã Check-in
            newPhieu.setThoiGianDat(LocalDateTime.now());
            newPhieu.setSoLuongKhach(1);
            newPhieu = phieuDatBanRepository.save(newPhieu);

            // B. Tạo Hóa Đơn gắn với Phiếu đó
            hoaDon = new HoaDonThanhToan();
            hoaDon.setIdPhieuDatBan(newPhieu); // 🚨 Mấu chốt kiến trúc mới nằm ở đây
            hoaDon.setThoiGianTao(Instant.now());
            hoaDon.setTrangThaiHoaDon(4); // 4 = Đang phục vụ
            hoaDon.setTongTienChuaGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

            if (req.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(req.getIdNhanVien()).orElse(null);
                hoaDon.setIdNhanVien(nv);
            }
            hoaDon = hoaDonThanhToanRepository.save(hoaDon);

            // Ghi log
            ghiLichSu(hoaDon, req.getIdNhanVien(), "Tạo hóa đơn", "Khách vãng lai vào bàn " + banAn.getMaBan(), null, 4);
        }

        // Đảm bảo tất cả các bàn trong phiếu này đều bật đèn đỏ "Có khách"
        if (hoaDon.getIdPhieuDatBan() != null) {
            for (BanAn ban : hoaDon.getIdPhieuDatBan().getBanAns()) {
                if (ban.getTrangThai() == 0 || ban.getTrangThai() == 2) {
                    ban.setTrangThai(1);
                    banAnRepo.save(ban);
                }
            }
        }

        Integer trangThaiHienTai = hoaDon.getTrangThaiHoaDon();

        // =======================================================
        // 2. ĐỒNG BỘ CHI TIẾT MÓN ĂN (Logic cũ của bạn giữ nguyên, rất chuẩn)
        // =======================================================
        List<ChiTietHoaDon> existingDetails = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
        if (existingDetails == null) existingDetails = new ArrayList<>();

        List<ChiTietHoaDon> itemsToKeep = new ArrayList<>();
        BigDecimal tongTienMoi = BigDecimal.ZERO;

        List<ChiTietHoaDonRequest> requestDetails = req.getChiTietHoaDon();
        if (requestDetails == null) requestDetails = new ArrayList<>();

        for (ChiTietHoaDonRequest itemReq : requestDetails) {
            Optional<ChiTietHoaDon> matchOpt = existingDetails.stream().filter(dbItem -> {
                if (itemReq.getId() != null && itemReq.getId().equals(dbItem.getId())) return true;
                boolean isFoodMatch = itemReq.getIdChiTietMonAn() != null && dbItem.getIdChiTietMonAn() != null
                        && itemReq.getIdChiTietMonAn().equals(dbItem.getIdChiTietMonAn().getId());
                boolean isSetMatch = itemReq.getIdSetLau() != null && dbItem.getIdSetLau() != null
                        && itemReq.getIdSetLau().equals(dbItem.getIdSetLau().getId());
                return isFoodMatch || isSetMatch;
            }).findFirst();

            ChiTietHoaDon chiTiet;
            BigDecimal donGiaGoc = BigDecimal.ZERO;

            // 🚨 BIẾN MỚI ĐỂ LƯU TỶ LỆ VAT (Mặc định 10%)
            Integer vatPhanTramCuaMon = 10;

            if (matchOpt.isPresent()) {
                chiTiet = matchOpt.get();
                donGiaGoc = chiTiet.getDonGiaTaiThoiDiemBan();

                // Lấy lại VAT của món cũ nếu có (hoặc lấy từ DM)
                if (chiTiet.getIdChiTietMonAn() != null && chiTiet.getIdChiTietMonAn().getDanhMuc() != null) {
                    vatPhanTramCuaMon = chiTiet.getIdChiTietMonAn().getDanhMuc().getLoaiVatApDung() == 2 ? 8 : 10;
                }

                if (chiTiet.getTrangThaiMon() != null && chiTiet.getTrangThaiMon() == 0) {
                    chiTiet.setTrangThaiMon(1);
                    ghiLichSu(hoaDon, req.getIdNhanVien(),
                            "Gọi lại món: " + getTenMonFromEntity(chiTiet) + " x" + itemReq.getSoLuong(),
                            "Khách gọi lại món đã hủy", trangThaiHienTai, trangThaiHienTai);
                }

                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());
            } else {
                chiTiet = new ChiTietHoaDon();
                chiTiet.setIdHoaDon(hoaDon);

                if (itemReq.getIdChiTietMonAn() != null) {
                    DanhMucChiTiet monAn = chiTietDanhMucChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID " + itemReq.getIdChiTietMonAn()));
                    chiTiet.setIdChiTietMonAn(monAn);
                    donGiaGoc = monAn.getGiaBan();

                    // 🚨 LẤY TỶ LỆ VAT TỪ DANH MỤC
                    if (monAn.getDanhMuc() != null && monAn.getDanhMuc().getLoaiVatApDung() != null) {
                        // Giả sử: 2 = VAT 8%, Còn lại = VAT 10%
                        vatPhanTramCuaMon = monAn.getDanhMuc().getLoaiVatApDung() == 2 ? 8 : 10;
                    }

                } else if (itemReq.getIdSetLau() != null) {
                    SetLau setLau = setLauRepository.findById(itemReq.getIdSetLau())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID " + itemReq.getIdSetLau()));
                    chiTiet.setIdSetLau(setLau);
                    donGiaGoc = setLau.getGiaBan();

                    // Set lẩu mặc định là đồ chế biến -> VAT 10%
                    vatPhanTramCuaMon = 10;
                } else {
                    throw new RuntimeException("Dữ liệu không hợp lệ: Phải truyền ID Món hoặc ID Set");
                }

                chiTiet.setDonGiaTaiThoiDiemBan(donGiaGoc);
                chiTiet.setTrangThaiMon(1);
                chiTiet.setNgayGioTao(LocalDateTime.now());
                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());

                ghiLichSu(hoaDon, req.getIdNhanVien(),
                        "Gọi món: " + getTenMonFromEntity(chiTiet) + " x" + itemReq.getSoLuong(),
                        "Khách gọi món", trangThaiHienTai, trangThaiHienTai);
            }

            // =========================================================
            // 🚨 TÍNH VÀ LƯU TIỀN VAT XUỐNG DB
            // =========================================================
            // 1. Tính tổng tiền gốc (Chưa VAT)
            BigDecimal thanhTien = donGiaGoc.multiply(BigDecimal.valueOf(itemReq.getSoLuong()));

            // 2. Tính số tiền VAT = thanhTien * (vatPhanTram / 100)
            BigDecimal tienVatCuaMon = thanhTien
                    .multiply(BigDecimal.valueOf(vatPhanTramCuaMon))
                    .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);

            // 3. Set vào Entity
            chiTiet.setThanhTien(thanhTien);
            chiTiet.setTienVat(tienVatCuaMon); // 🚨 Lưu tiền VAT (Ví dụ: 304100)

            // Lưu xuống DB
            chiTiet = chiTietHoaDonRepository.save(chiTiet);
            itemsToKeep.add(chiTiet);
            tongTienMoi = tongTienMoi.add(thanhTien);
        }

        // XÓA MỀM
        List<ChiTietHoaDon> toDelete = existingDetails.stream()
                .filter(oldItem -> oldItem.getTrangThaiMon() != null && oldItem.getTrangThaiMon() != 0)
                .filter(oldItem -> !itemsToKeep.contains(oldItem))
                .collect(Collectors.toList());

        for (ChiTietHoaDon delItem : toDelete) {
            ghiLichSu(hoaDon, req.getIdNhanVien(),
                    "Hủy món: " + getTenMonFromEntity(delItem) + " x" + delItem.getSoLuong(),
                    "Nhân viên xóa trực tiếp", trangThaiHienTai, trangThaiHienTai);
            delItem.setTrangThaiMon(0);
            chiTietHoaDonRepository.save(delItem);
        }

        // =======================================================
        // 3. TÍNH TOÁN LẠI TÀI CHÍNH
        // =======================================================
        List<ChiTietHoaDon> dsMonHienTai = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId())
                .stream().filter(item -> item.getTrangThaiMon() != null && item.getTrangThaiMon() != 0)
                .collect(Collectors.toList());

        // Tính tổng tiền gốc của các món
        BigDecimal tongTienChuaGiam = dsMonHienTai.stream()
                .map(ChiTietHoaDon::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🚨 TÍNH TỔNG TIỀN VAT BẰNG CÁCH CỘNG DỒN TỪ TỪNG MÓN ĂN
        BigDecimal tongTienVat = dsMonHienTai.stream()
                .map(item -> item.getTienVat() != null ? item.getTienVat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienChuaGiam(tongTienChuaGiam);

        hoaDon.setVatApDung(tongTienVat);

        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // Kiểm tra Voucher
        phieuGiamGiaService.kiemTraLaiDieuKienVoucher(hoaDon.getId());
        hoaDon = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElseThrow();

        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;

        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        // 🚨 CỘNG TRỰC TIẾP TIỀN VAT (FLAT) VÀO TỔNG THANH TOÁN (KHÔNG NHÂN % NỮA)
        BigDecimal tongTienThanhToan = tienSauGiam.add(tongTienVat).subtract(tienCoc);

        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) tongTienThanhToan = BigDecimal.ZERO;

        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDonThanhToanRepository.save(hoaDon);
    }

    private String getTenMonFromEntity(ChiTietHoaDon ct) {
        if (ct.getIdChiTietMonAn() != null) return ct.getIdChiTietMonAn().getTenMon();
        if (ct.getIdSetLau() != null) return ct.getIdSetLau().getTenSetLau();
        return "Món không xác định";
    }

    private void ghiLichSu(HoaDonThanhToan hoaDon, Integer idNhanVien, String hanhDong, String lyDo, Integer trangThaiCu, Integer trangThaiMoi) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setIdHoaDon(hoaDon);
        if (idNhanVien != null) {
            NhanVien nv = nhanVienRepository.findById(idNhanVien).orElse(null);
            lichSu.setIdNhanVien(nv);
        }
        lichSu.setHanhDong(hanhDong);
        lichSu.setLyDoThucHien(lyDo);
        lichSu.setThoiGianThucHien(Instant.now());
        lichSu.setTrangThaiTruocDo(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThaiMoi);
        lichSuHoaDonRepository.save(lichSu);
    }

    // Thêm vào HoaDonThanhToanService.java

    @Transactional
    public void gopBan(GopBanRequest req) {
        Integer idChu = req.getIdHoaDonChu();
        Integer idBiNuot = req.getIdHoaDonBiNuot();

        HoaDonThanhToan hdChu = hoaDonThanhToanRepository.findById(idChu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn Chủ"));
        HoaDonThanhToan hdBiNuot = hoaDonThanhToanRepository.findById(idBiNuot)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn bị nuốt"));

        PhieuDatBan phieuChu = hdChu.getIdPhieuDatBan();
        PhieuDatBan phieuBiNuot = hdBiNuot.getIdPhieuDatBan();

        // =========================================================
        // 1. CHUYỂN BÀN BẰNG CƠ CHẾ JPA (Khắc phục lỗi tàng hình bàn trong JSON)
        // =========================================================
        Set<BanAn> banCuaPhieuPhu = new HashSet<>(phieuBiNuot.getBanAns());
        for (BanAn ban : banCuaPhieuPhu) {
            phieuBiNuot.getBanAns().remove(ban); // Gỡ khỏi phiếu bị nuốt
            phieuChu.getBanAns().add(ban);       // Gắn sang phiếu chủ
        }

        // =========================================================
        // 2. CHUYỂN MÓN ĂN BẰNG CƠ CHẾ JPA
        // =========================================================
        List<ChiTietHoaDon> listMonPhu = chiTietHoaDonRepository.findByIdHoaDon_Id(idBiNuot);
        if (listMonPhu != null && !listMonPhu.isEmpty()) {
            for (ChiTietHoaDon mon : listMonPhu) {
                mon.setIdHoaDon(hdChu); // Chuyển chủ cho từng đĩa thịt/cốc nước
            }
            chiTietHoaDonRepository.saveAll(listMonPhu);
        }

        // =========================================================
        // 3. CHUYỂN LỊCH SỬ CỌC (Vẫn dùng Native Query vì không ảnh hưởng JSON Response)
        // =========================================================
        lichSuThanhToanRepository.chuyenLichSuThanhToanSangHoaDonMoi(idChu, idBiNuot);

        // =========================================================
        // 4. DỒN TIỀN CỌC
        // =========================================================
        BigDecimal cocChu = hdChu.getTienCoc() != null ? hdChu.getTienCoc() : BigDecimal.ZERO;
        BigDecimal cocNuot = hdBiNuot.getTienCoc() != null ? hdBiNuot.getTienCoc() : BigDecimal.ZERO;
        hdChu.setTienCoc(cocChu.add(cocNuot));

        // =========================================================
        // 5. ĐÓNG BĂNG HÓA ĐƠN BỊ NUỐT
        // =========================================================
        Integer trangThaiCuBiNuot = hdBiNuot.getTrangThaiHoaDon();
        hdBiNuot.setTienCoc(BigDecimal.ZERO);
        hdBiNuot.setTongTienChuaGiam(BigDecimal.ZERO);
        hdBiNuot.setTongTienThanhToan(BigDecimal.ZERO);
        hdBiNuot.setSoTienDaGiam(BigDecimal.ZERO);
        hdBiNuot.setTrangThaiHoaDon(8); // 8: Trạng thái ĐÃ HỦY
        hdBiNuot.setGhiChu("[GỘP BÀN] Toàn bộ dữ liệu chuyển sang HD: " + hdChu.getMaHoaDon());
        phieuBiNuot.setTrangThai(2); // 2: Phiếu ĐÃ HỦY

        // =========================================================
        // 6. GHI LOG CHO 2 BÊN
        // =========================================================
        ghiLichSu(hdChu, req.getIdNhanVien(), "Gộp bàn", "Nhận dữ liệu từ HD " + hdBiNuot.getMaHoaDon(), hdChu.getTrangThaiHoaDon(), hdChu.getTrangThaiHoaDon());
        ghiLichSu(hdBiNuot, req.getIdNhanVien(), "Bị gộp bàn", "Chuyển dữ liệu sang HD " + hdChu.getMaHoaDon(), trangThaiCuBiNuot, 8);

        // =========================================================
        // 7. LƯU MỌI THỨ VÀ TÍNH TIỀN
        // =========================================================
        phieuDatBanRepository.save(phieuBiNuot);
        phieuDatBanRepository.save(phieuChu);
        hoaDonThanhToanRepository.save(hdBiNuot);
        hoaDonThanhToanRepository.save(hdChu);

        // Hàm tính lại tiền của bạn sẽ đọc danh sách món ăn đã cập nhật
        tinhLaiTienHoaDon(hdChu);
    }

    // Hàm phụ trợ dùng để Tính toán lại tiền (Dùng lại logic của bạn)
    private void tinhLaiTienHoaDon(HoaDonThanhToan hoaDon) {
        List<ChiTietHoaDon> dsMonHienTai = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId())
                .stream().filter(item -> item.getTrangThaiMon() != null && item.getTrangThaiMon() != 0)
                .collect(Collectors.toList());

        // Tổng tiền gốc
        BigDecimal tongTienChuaGiam = dsMonHienTai.stream()
                .map(ChiTietHoaDon::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🚨 TỔNG TIỀN VAT
        BigDecimal tongTienVat = dsMonHienTai.stream()
                .map(item -> item.getTienVat() != null ? item.getTienVat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienChuaGiam(tongTienChuaGiam);

        hoaDon.setVatApDung(tongTienVat);

        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // Kiểm tra xem bill sau khi gộp có làm rớt mã giảm giá không
        phieuGiamGiaService.kiemTraLaiDieuKienVoucher(hoaDon.getId());
        hoaDon = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElseThrow();

        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;

        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        // 🚨 CỘNG TRỰC TIẾP TIỀN VAT
        BigDecimal tongTienThanhToan = tienSauGiam.add(tongTienVat).subtract(tienCoc);

        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) tongTienThanhToan = BigDecimal.ZERO;

        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDonThanhToanRepository.save(hoaDon);
    }

    // Thêm hàm này vào HoaDonThanhToanService.java

    @Transactional
    public void themBanVaoHoaDon(Integer idHoaDonGoc, Integer idBanMoi) {
        // 1. Tìm Hóa đơn gốc và Bàn mới
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(idHoaDonGoc)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn gốc"));

        BanAn banMoi = banAnRepo.findById(idBanMoi)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bàn ăn mới"));

        // Kiểm tra xem bàn mới có đang trống không
        if (banMoi.getTrangThai() != 0) {
            throw new RuntimeException("Bàn " + banMoi.getMaBan() + " không trống, không thể ghép vào đoàn!");
        }

        // 2. Gắn Bàn mới vào Phiếu (Logic N-N)
        PhieuDatBan phieu = hd.getIdPhieuDatBan();
        if (phieu == null) {
            throw new RuntimeException("Hóa đơn này không có phiếu liên kết!");
        }

        phieu.getBanAns().add(banMoi);
        phieuDatBanRepository.save(phieu);

        // 3. Đổi trạng thái Bàn mới thành Có khách (1)
        banMoi.setTrangThai(1);
        banAnRepo.save(banMoi);

        // 4. Ghi Log Lịch sử Hóa đơn
        ghiLichSu(hd, null, "Mở rộng bàn", "Xếp thêm đoàn vào Bàn " + banMoi.getMaBan(), hd.getTrangThaiHoaDon(), hd.getTrangThaiHoaDon());
    }
}