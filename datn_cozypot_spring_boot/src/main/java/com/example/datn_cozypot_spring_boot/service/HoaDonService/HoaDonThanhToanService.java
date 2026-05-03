package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.GopBanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.request.EmailHuyDatBanDTO;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import com.example.datn_cozypot_spring_boot.service.EmailDatBanService;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
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
    EmailDatBanService emailDatBanService;

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
    @Autowired
    private phieuDatBanBanAnRepository phieuDatBanBanAnRepository;
    @Autowired
    private ChiTietKhuyenMaiSetRepository chiTietKhuyenMaiSetRepository;
    @Autowired
    private ChiTietKhuyenMaiMonRepository chiTietKhuyenMaiMonRepository;

    public Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable){
        return hoaDonThanhToanRepository.getAllHoaDon(pageable);
    }

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Instant tuNgayTao, Instant denNgayTao, LocalDateTime tuNgayDat, LocalDateTime denNgayDat, Pageable pageable) {
        // 1. Lấy trang dữ liệu thô (chưa có mảng bàn) từ Repository
        Page<HoaDonThanhToanResponse> page = hoaDonThanhToanRepository.searchHoaDon(key, trangThai, tuNgayTao, denNgayTao, tuNgayDat, denNgayDat, pageable);

        // 2. Với mỗi kết quả, đi tìm danh sách bàn tương ứng
        page.getContent().forEach(res -> {
            // Tìm lại Entity HoaDon để truy cập vào Set bàn (hoặc dùng query riêng để tối ưu)
            HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(res.getId()).orElse(null);
            if (hd != null && hd.getIdPhieuDatBan() != null) {
                List<String> tenBans = hd.getIdPhieuDatBan().getBanAns()
                        .stream()
                        .map(BanAn::getMaBan) // Hoặc getTenBan() tùy bạn
                        .collect(Collectors.toList());
                res.setDanhSachTenBan(tenBans);
            } else {
                res.setDanhSachTenBan(new ArrayList<>());
            }
        });

        return page;
    }

    public HoaDonThanhToanResponse getHoaDonById(Integer id) {
        HoaDonThanhToanResponse res = hoaDonThanhToanRepository.getHoaDonById(id);

        if (res != null) {
            HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(id).orElse(null);
            if (hd != null && hd.getIdPhieuDatBan() != null) {
                List<String> tenBans = hd.getIdPhieuDatBan().getBanAns()
                        .stream()
                        .map(BanAn::getMaBan)
                        .collect(Collectors.toList());
                res.setDanhSachTenBan(tenBans);
            } else {
                res.setDanhSachTenBan(new ArrayList<>());
            }
        }

        return res;
    }

    @Transactional
    public void huyHoaDon(LichSuHoaDonRequest request) {
        if (chiTietHoaDonService.hasAnyDishServed(request.getIdHoaDon())) {
            throw new RuntimeException("Không thể hủy hóa đơn vì đã có món ăn được phục vụ!");
        }

        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu == 7 || trangThaiHDCu == 8 || trangThaiHDCu == 9 || trangThaiHDCu == 10) {
            throw new RuntimeException("Hóa đơn đã ở trạng thái kết thúc, không thể hủy!");
        }

        BigDecimal tienCoc = hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO;
        int trangThaiMoi;

        if (trangThaiHDCu >= 2 && tienCoc.compareTo(BigDecimal.ZERO) > 0 &&
                (Boolean.TRUE.equals(request.getIsLoiDoQuan()) || trangThaiHDCu == 2)) {

            hd.setTienHoanTra(tienCoc);
            trangThaiMoi = 9;
            hd.setTrangThaiHoaDon(trangThaiMoi);

//            LichSuThanhToan lsThanhToan = new LichSuThanhToan();
//            lsThanhToan.setHoaDon(hd);
//            lsThanhToan.setSoTienThanhToan(tienCoc);
//            lsThanhToan.setLoaiGiaoDich(3);
//
//            // Đặt câu thông báo chi tiết cho lịch sử hoàn tiền
//            if (trangThaiHDCu == 2) {
//                lsThanhToan.setGhiChu("Hoàn trả cọc 100% do hóa đơn chưa được xác nhận: " + request.getLyDoThucHien());
//            } else {
//                lsThanhToan.setGhiChu("Hoàn trả cọc do lỗi của quán: " + request.getLyDoThucHien());
//            }
//
//            lsThanhToan.setNgayThanhToan(Instant.now());
//            lsThanhToan.setTrangThai(1);
//            lsThanhToan.setTenPhuongThuc("Chuyển khoản");
//            String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
//            lsThanhToan.setMaGiaoDich("REFUND_" + maHoaDonStr + "_" + System.currentTimeMillis());
//            lichSuThanhToanRepository.save(lsThanhToan);
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
        PhieuDatBan phieu = hd.getIdPhieuDatBan();
        if (phieu != null) {
            phieu.setTrangThai(2); // Hủy phiếu

            for (BanAn ban : phieu.getBanAns()) {
                // 1. Lấy danh sách tất cả các phiếu đang mượn bàn này
                List<PhieuDatBanBanAn> cacPhieuCungBan = phieuDatBanBanAnRepository.findActiveLinksByBanId(ban.getId());

                // 2. Đếm xem có phiếu nào KHÁC với phiếu đang bị hủy này không
                long countOtherActive = cacPhieuCungBan.stream()
                        .filter(l -> !l.getPhieuDatBan().getId().equals(phieu.getId()))
                        .count();

                // 3. 🚨 CHỈ DỌN BÀN VỀ 0 NẾU KHÔNG CÒN ĐOÀN KHÁCH NÀO KHÁC
                if (countOtherActive == 0) {
                    ban.setTrangThai(0); // Bàn về trạng thái Trống
                    banAnRepo.save(ban); // Lưu ý: Nếu báo đỏ thì đổi thành banAnRepository.save(ban)
                }
            }
            phieuDatBanRepository.save(phieu);
        }

        hoaDonThanhToanRepository.save(hd);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(trangThaiMoi == 9 ? "Hủy & Chờ hoàn tiền" : "Hủy hóa đơn");
        String prefixLyDo = "";
        if (trangThaiMoi == 9) {
            prefixLyDo = "[Chờ hoàn cọc] ";
        } else if (trangThaiHDCu >= 2 && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = "[Mất cọc] ";
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

        // GỬI EMAIL THÔNG BÁO HỦY (SAU KHI ĐÃ LƯU DB)
        var khachHang = hd.getIdKhachHang();
        if (khachHang != null && khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {

            // Format tiền tệ nếu có hoàn trả (Trạng thái 9)
            String tienHoanTraMail = null;
            if (trangThaiMoi == 9 && hd.getTienHoanTra() != null) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tienHoanTraMail = currencyFormat.format(hd.getTienHoanTra());
            }

            // Lấy mã phiếu hoặc mã hóa đơn
            String maGiaoDichMail = (phieu != null && phieu.getMaDatBan() != null) ? phieu.getMaDatBan() : hd.getMaHoaDon();

            String nguoiHuyMail = "Nhà hàng CozyPot"; // Mặc định nếu hệ thống tự hủy
            if (request.getIdNhanVien() != null) {
                NhanVien nhanVienDb = nhanVienRepository.findById(request.getIdNhanVien()).orElse(null);
                if (nhanVienDb != null) {
                    String maNV = nhanVienDb.getMaNhanVien() != null ? nhanVienDb.getMaNhanVien() : "NV";
                    String tenNV = nhanVienDb.getHoTenNhanVien() != null ? nhanVienDb.getHoTenNhanVien() : "";
                    nguoiHuyMail = maNV + " - " + tenNV;
                }
            }

            EmailHuyDatBanDTO mailData = EmailHuyDatBanDTO.builder()
                    .email(khachHang.getEmail())
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .maPhieuDatBan(maGiaoDichMail)
                    .nguoiHuy(nguoiHuyMail)
                    .lyDoHuy(request.getLyDoThucHien())
                    .tienHoanTra(tienHoanTraMail)
                    .build();

            emailDatBanService.sendEmailHuyDatBan(mailData);
        }
    }

    @Transactional
    public void xacNhanHoanTien(Integer idHoaDon, Integer idNhanVien) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        if (hd.getTrangThaiHoaDon() != 9) {
            throw new RuntimeException("Hóa đơn không ở trạng thái Chờ hoàn tiền!");
        }

        // 1. Chuyển trạng thái sang Đã hoàn tiền (10)
        hd.setTrangThaiHoaDon(10);
        hoaDonThanhToanRepository.save(hd);

        // 2. Lưu Lịch sử thanh toán (Tiền cọc trả khách)
        BigDecimal tienHoanTra = hd.getTienHoanTra() != null ? hd.getTienHoanTra() : BigDecimal.ZERO;
        LichSuThanhToan lsThanhToan = new LichSuThanhToan();
        lsThanhToan.setHoaDon(hd);
        lsThanhToan.setSoTienThanhToan(tienHoanTra);
        lsThanhToan.setLoaiGiaoDich(3);
        lsThanhToan.setGhiChu("Kế toán xác nhận đã chuyển khoản hoàn tiền cho khách hàng");
        lsThanhToan.setNgayThanhToan(Instant.now());
        lsThanhToan.setTrangThai(1);
        lsThanhToan.setTenPhuongThuc("Chuyển khoản");
        lsThanhToan.setGhiChu("Hoàn trả tiền cọc cho hóa đơn " + hd.getMaHoaDon());
        String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
        lsThanhToan.setMaGiaoDich("REFUND_" + maHoaDonStr + "_" + System.currentTimeMillis());
        lichSuThanhToanRepository.save(lsThanhToan);

        // 3. Ghi log Lịch sử hóa đơn
        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Xác nhận đã hoàn tiền");
        log.setLyDoThucHien("[Đã hoàn tiền] Hoàn tất hoàn tiền cho khách hàng");
        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(9);
        log.setTrangThaiMoi(10);

        if (idNhanVien != null) {
            NhanVien nv = new NhanVien();
            nv.setId(idNhanVien);
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

        if (req.getIdHoaDon() != null) {
            hoaDon = hoaDonThanhToanRepository.findById(req.getIdHoaDon()).orElse(null);
        } else {
            List<PhieuDatBan> listPhieu = phieuDatBanRepository.findByDsBanAn_BanAn_IdAndTrangThaiInOrderByThoiGianDatAsc(req.getIdBanAn(), Arrays.asList(0, 1, 3));
            if (!listPhieu.isEmpty()) {
                PhieuDatBan phieuActive = listPhieu.get(0);
                hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieuActive.getId());
            }
        }

        if (hoaDon == null) {
            PhieuDatBan newPhieu = new PhieuDatBan();
            newPhieu.setTrangThai(3);
            newPhieu.setThoiGianDat(LocalDateTime.now());
            newPhieu.setSoLuongKhach(1);
            newPhieu = phieuDatBanRepository.save(newPhieu);

            PhieuDatBanBanAn linkGoc = new PhieuDatBanBanAn();
            linkGoc.setPhieuDatBan(newPhieu);
            linkGoc.setBanAn(banAn);
            PhieuDatBanBanAnId linkId = new PhieuDatBanBanAnId();
            linkId.setIdPhieuDatBan(newPhieu.getId());
            linkId.setIdBanAn(banAn.getId());
            linkGoc.setId(linkId);

            if (newPhieu.getDsBanAn() == null) newPhieu.setDsBanAn(new HashSet<>());
            newPhieu.getDsBanAn().add(linkGoc);
            phieuDatBanRepository.save(newPhieu);
            phieuDatBanRepository.flush();

            hoaDon = new HoaDonThanhToan();
            hoaDon.setIdPhieuDatBan(newPhieu);
            hoaDon.setThoiGianTao(Instant.now());
            hoaDon.setTrangThaiHoaDon(4);
            hoaDon.setTongTienChuaGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

            if (req.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(req.getIdNhanVien()).orElse(null);
                hoaDon.setIdNhanVien(nv);
            }
            hoaDon = hoaDonThanhToanRepository.save(hoaDon);
        }

        if (hoaDon.getIdPhieuDatBan() != null) {
            for (PhieuDatBanBanAn link : hoaDon.getIdPhieuDatBan().getDsBanAn()) {
                BanAn ban = link.getBanAn();
                if (ban.getTrangThai() == 0 || ban.getTrangThai() == 2) {
                    ban.setTrangThai(1);
                    banAnRepo.save(ban);
                }
            }
        }

        Integer trangThaiHienTai = hoaDon.getTrangThaiHoaDon();
        LocalDate today = LocalDate.now();
        List<ChiTietHoaDon> existingDetails = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
        List<ChiTietHoaDonRequest> requestDetails = req.getChiTietHoaDon();

        if (requestDetails != null) {
            for (ChiTietHoaDonRequest itemReq : requestDetails) {

                // 🚨 CỰC KỲ QUAN TRỌNG: NẾU SỐ LƯỢNG LÀ 0 THÌ HIỂU LÀ KHÁCH XÓA MÓN ĐÓ
                if (itemReq.getSoLuong() <= 0 && itemReq.getId() != null) {
                    HoaDonThanhToan finalHoaDon = hoaDon;
                    existingDetails.stream()
                            .filter(dbItem -> itemReq.getId().equals(dbItem.getId()))
                            .findFirst()
                            .ifPresent(delItem -> {
                                delItem.setTrangThaiMon(0);
                                chiTietHoaDonRepository.save(delItem);
                                ghiLichSu(finalHoaDon, req.getIdNhanVien(), "Hủy món", "Nhân viên xóa món", trangThaiHienTai, trangThaiHienTai);
                            });
                    continue; // Chuyển sang món tiếp theo
                }

                // Nếu số lượng > 0 thì tiếp tục
                Optional<ChiTietHoaDon> matchOpt = existingDetails.stream().filter(dbItem ->
                        itemReq.getId() != null && itemReq.getId().equals(dbItem.getId())
                ).findFirst();

                ChiTietHoaDon chiTiet;
                BigDecimal donGiaThucTe = BigDecimal.ZERO;

                if (matchOpt.isPresent()) {
                    chiTiet = matchOpt.get();
                    donGiaThucTe = chiTiet.getDonGiaTaiThoiDiemBan();

                    chiTiet.setSoLuong(itemReq.getSoLuong());
                    chiTiet.setGhiChuMon(itemReq.getGhiChu());

                    BigDecimal thanhTien = donGiaThucTe.multiply(BigDecimal.valueOf(itemReq.getSoLuong()));
                    chiTiet.setThanhTien(thanhTien);

                    chiTietHoaDonRepository.save(chiTiet);
                } else {
                    chiTiet = new ChiTietHoaDon();
                    chiTiet.setIdHoaDon(hoaDon);

                    if (itemReq.getIdChiTietMonAn() != null) {
                        DanhMucChiTiet monAn = chiTietDanhMucChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
                        chiTiet.setIdChiTietMonAn(monAn);

                        BigDecimal giaNiemYet = monAn.getGiaBan();
                        List<Integer> danhSachGiamGia = chiTietKhuyenMaiMonRepository.findActiveDiscount(monAn.getId(), today);
                        Integer phanTramGiam = danhSachGiamGia.stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(0);

                        if (phanTramGiam > 0) {
                            donGiaThucTe = giaNiemYet.multiply(BigDecimal.valueOf(100 - phanTramGiam)).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
                        } else {
                            donGiaThucTe = giaNiemYet;
                        }

                    } else if (itemReq.getIdSetLau() != null) {
                        SetLau setLau = setLauRepository.findById(itemReq.getIdSetLau())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu"));
                        chiTiet.setIdSetLau(setLau);

                        BigDecimal giaNiemYet = setLau.getGiaBan();
                        List<Integer> danhSachGiamGiaSet = chiTietKhuyenMaiSetRepository.findActiveDiscount(setLau.getId(), today);
                        Integer phanTramGiam = danhSachGiamGiaSet.stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(0);

                        if (phanTramGiam > 0) {
                            donGiaThucTe = giaNiemYet.multiply(BigDecimal.valueOf(100 - phanTramGiam)).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
                        } else {
                            donGiaThucTe = giaNiemYet;
                        }
                    }

                    chiTiet.setDonGiaTaiThoiDiemBan(donGiaThucTe);
                    chiTiet.setTrangThaiMon(1); // 🚨 LUÔN LUÔN CHỜ LÊN
                    chiTiet.setNgayGioTao(LocalDateTime.now());
                    chiTiet.setSoLuong(itemReq.getSoLuong());
                    chiTiet.setGhiChuMon(itemReq.getGhiChu());

                    BigDecimal thanhTien = donGiaThucTe.multiply(BigDecimal.valueOf(itemReq.getSoLuong()));
                    chiTiet.setThanhTien(thanhTien);

                    chiTietHoaDonRepository.save(chiTiet);
                    ghiLichSu(hoaDon, req.getIdNhanVien(), "Gọi món", "Khách gọi thêm món", trangThaiHienTai, trangThaiHienTai);
                }
            }
        }

        // =======================================================
        // 4. TÍNH TOÁN LẠI TÀI CHÍNH TỔNG (Dựa vào Database)
        // =======================================================
        List<ChiTietHoaDon> dsMonHienTai = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());

        BigDecimal tongTienChuaGiam = dsMonHienTai.stream()
                .filter(item -> item.getTrangThaiMon() != null && item.getTrangThaiMon() != 0)
                .map(ChiTietHoaDon::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienChuaGiam(tongTienChuaGiam);
        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // Kiểm tra Voucher
        phieuGiamGiaService.kiemTraLaiDieuKienVoucher(hoaDon.getId());
        hoaDon = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElseThrow();

        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;

        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        BigDecimal tongTienThanhToan = tienSauGiam.subtract(tienCoc);
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
//        BigDecimal tongTienVat = dsMonHienTai.stream()
//                .map(item -> item.getTienVat() != null ? item.getTienVat() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienChuaGiam(tongTienChuaGiam);

//        hoaDon.setVatApDung(tongTienVat);

        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // Kiểm tra xem bill sau khi gộp có làm rớt mã giảm giá không
        phieuGiamGiaService.kiemTraLaiDieuKienVoucher(hoaDon.getId());
        hoaDon = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElseThrow();

        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;

        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        // 🚨 CỘNG TRỰC TIẾP TIỀN VAT
        BigDecimal tongTienThanhToan = tienSauGiam.subtract(tienCoc); // Gỡ bỏ .add(tongTienVat)
        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) tongTienThanhToan = BigDecimal.ZERO;

        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDonThanhToanRepository.save(hoaDon);
    }

    // Thêm hàm này vào HoaDonThanhToanService.java

    @Transactional
    public void themBanVaoHoaDon(Integer idHoaDonGoc, Integer idBanMoi, Integer soNguoiNgoi) {
        // 1. Tìm Hóa đơn gốc và Bàn mới
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(idHoaDonGoc)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn gốc"));

        BanAn banMoi = banAnRepo.findById(idBanMoi)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bàn ăn mới"));

        // 2. Kiểm tra trạng thái vật lý
        if (banMoi.getTrangThai() != 0) {
            throw new RuntimeException("Bàn " + banMoi.getMaBan() + " không trống, không thể ghép vào đoàn!");
        }

        PhieuDatBan phieu = hd.getIdPhieuDatBan();
        if (phieu == null) {
            throw new RuntimeException("Hóa đơn này không có phiếu liên kết!");
        }

        // 3. KIỂM TRA TRÙNG LỊCH
        LocalDateTime thoiGianHienTai = LocalDateTime.now();
        LocalDateTime thoiGianKetThucDuKien = thoiGianHienTai.plusHours(3);

        boolean isConflict = phieuDatBanRepository.existsByBanAnIdAndTimeRangeAndStatus(
                banMoi.getId(),
                phieu.getId(),
                thoiGianHienTai.minusHours(1),
                thoiGianKetThucDuKien,
                Arrays.asList(0, 1, 3)
        );

        if (isConflict) {
            throw new RuntimeException("Không thể ghép! Bàn " + banMoi.getMaBan() + " đã có khách đặt trước trong khung giờ này.");
        }

        // 4. KIỂM TRA HÓA ĐƠN TÀNG HÌNH
        Optional<HoaDonThanhToan> existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(banMoi.getId());
        if (existingBill.isPresent()) {
            throw new RuntimeException("Lỗi dữ liệu: Bàn này đang bị kẹt ở Hóa đơn #" + existingBill.get().getMaHoaDon());
        }

        // =========================================================
        // 5. TẠO VÀ LƯU TRỰC TIẾP LIÊN KẾT BẢNG TRUNG GIAN N-N
        // =========================================================
        PhieuDatBanBanAn linkMoi = new PhieuDatBanBanAn();
        linkMoi.setPhieuDatBan(phieu);
        linkMoi.setBanAn(banMoi);
        linkMoi.setSoNguoiNgoi(soNguoiNgoi != null ? soNguoiNgoi : 0);

        PhieuDatBanBanAnId linkId = new PhieuDatBanBanAnId();
        linkId.setIdPhieuDatBan(phieu.getId());
        linkId.setIdBanAn(banMoi.getId());
        linkMoi.setId(linkId);

        // 1. Lưu xuống DB và HỨNG LẠI object đã được Hibernate quản lý (Managed Entity)
        PhieuDatBanBanAn savedLink = phieuDatBanBanAnRepository.saveAndFlush(linkMoi);

        // 2. Dọn dẹp rác (nếu có) để đảm bảo không bị double link trong RAM
        phieu.getDsBanAn().removeIf(l -> l.getBanAn().getId().equals(banMoi.getId()));

        // 3. Add chính cái Object đã quản lý đó vào danh sách của phiếu
        phieu.getDsBanAn().add(savedLink);
        // =========================================================

        // 6. Đổi trạng thái Bàn mới thành Có khách (1)
        banMoi.setTrangThai(1);
        banAnRepo.save(banMoi);

        // 7. ÉP HIBERNATE LƯU NGAY XUỐNG SQL (Triệt tiêu độ trễ API)
        phieuDatBanRepository.flush(); // Có thể giữ hoặc bỏ nếu không sửa phiếu
        banAnRepo.flush();

        // 8. Ghi Log Lịch sử Hóa đơn
        ghiLichSu(hd, null, "Mở rộng bàn", "Xếp thêm đoàn vào Bàn " + banMoi.getMaBan(), hd.getTrangThaiHoaDon(), hd.getTrangThaiHoaDon());
    }

    @Transactional
    public void updateTrangThaiHoaDonByIdPhieu(Integer idPhieu, Integer trangThaiMoi) {
        // 1. Tìm hóa đơn đang gắn với cái Phiếu đặt bàn này
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(idPhieu);

        // 2. Nếu tìm thấy thì cập nhật trạng thái
        if (hoaDon != null) {
            // 🚨 LẤY TRẠNG THÁI CŨ TRƯỚC KHI GHI ĐÈ LÊN
            Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

            // Cập nhật trạng thái mới
            hoaDon.setTrangThaiHoaDon(trangThaiMoi);
            hoaDonThanhToanRepository.save(hoaDon);

            // 🚨 GỌI HÀM GHI LỊCH SỬ (Truyền null cho nhân viên để hiểu là Hệ thống tự động xử lý)
            ghiLichSu(
                    hoaDon,
                    null,
                    "Hủy hóa đơn tự động",
                    "Hệ thống tự động hủy hóa đơn do Phiếu đặt bàn liên kết đã bị hủy",
                    trangThaiCu,
                    trangThaiMoi
            );
        }
    }
}