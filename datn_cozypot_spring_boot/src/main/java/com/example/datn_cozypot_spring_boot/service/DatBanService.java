package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanRequest;
import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.dto.response.BanTrangThaiResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
import com.example.datn_cozypot_spring_boot.dto.response.KhuVucResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.thanhToanRepository.PhuongThucThanhToanRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatBanService {
    @Autowired
    private BanAnRepository banAnRepository;

    @Autowired
    private PhieuDatBanRepository phieuDatBanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;
    @Autowired
    private HoaDonThanhToanRepository hoaDonThanhToanRepository;
    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private EmailDatBanService emailDatBanService;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository danhMucChiTietRepository;

    @Autowired
    private com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository setLauRepository;
    @Autowired
    private ThamSoHeThongRepository thamSoHeThongRepository;

    public List<DatBanListResponse> getAll(){
        return phieuDatBanRepository.findAll().stream().map(DatBanListResponse::new).toList();
    }


    public List<DatBanListResponse> getAllByTrangThai(){
        LocalDateTime thoiGianBatDau = LocalDateTime.now().minusHours(3); // Cho phép trễ tối đa 3 tiếng
        LocalDateTime thoiGianKetThuc = LocalDateTime.now().plusDays(2).toLocalDate().atTime(23, 59, 59); // Lấy cho cả ngày mai

        return phieuDatBanRepository.findWaitingListToday(thoiGianBatDau, thoiGianKetThuc)
                .stream()
                .map(DatBanListResponse::new)
                .toList();
    }

    public List<BanAnResponse> getAllBanAn(){
        List<BanAn> allBan = banAnRepository.findAll();

        // Lấy danh sách các phiếu đặt bàn "ảnh hưởng đến hiện tại"
        List<DatBanListResponse> waitingToday = getAllByTrangThai();

        return allBan.stream().map(ban -> {
            BanAnResponse res = new BanAnResponse(ban);

            // 1. Kiểm tra xem bàn này có nằm trong danh sách "Sắp có khách đến" không
            boolean isReservedToday = waitingToday.stream()
                    .anyMatch(p -> p.getMaBan() != null && p.getMaBan().contains(ban.getMaBan())); // Sửa contains vì 1 phiếu có thể nhiều bàn

            // 2. Định nghĩa lại trạng thái hiển thị
            if (ban.getTrangThai() == 1) {
                res.setTrangThai(1);
            } else if (isReservedToday) {
                res.setTrangThai(2);
            } else {
                res.setTrangThai(0);
            }
            return res;
        }).toList();
    }

    public BanAnResponse getBanAnById(Integer id){
        BanAn banAn =  banAnRepository.findById(id).get();
        return new BanAnResponse(banAn);
    }

    public List<KhuVucResponse> getAllKhuVuc(){
        return khuVucRepository.findAll().stream().map(KhuVucResponse::new).toList();
    }

    @Transactional
    public void addBanAn(AddBanAnRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        BanAn banAn = new BanAn();
        banAn.setIdKhuVuc(khuVuc);
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(0);
        banAn.setLoaiDatBan(req.getLoaiDatBan());
        banAn.setNguoiTao(username);
        banAnRepository.save(banAn);
    }

    @Transactional
    public void updateBanAn(UpdateBanAnRequest req) {

        BanAn banAn = banAnRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        banAn.setMaBan(req.getMaBan());
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(req.getTrangThai());     // ✔ lấy từ FE
        banAn.setLoaiDatBan(req.getLoaiDatBan());
        banAn.setIdKhuVuc(khuVuc);                   // ✔ set quan hệ

        banAnRepository.save(banAn); // ✔ UPDATE
    }

    public Page<DatBanListResponse> searchDatBan(DatBanSearchRequest request, Pageable pageable) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (request.getThoiGianDat() != null) {
            LocalDate date = request.getThoiGianDat();
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        }

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());

        Page<PhieuDatBan> page = phieuDatBanRepository.search(request.getSoDienThoai(), request.getTrangThai(), start, end, pageRequest);

        return page.map(DatBanListResponse::new);
    }

    @Transactional
    public void updateBanChoPhieu(DatBanUpdateRequest req) {
        // 🚨 UPDATE FOR N-N
        BanAn banAnMoi = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));


        PhieuDatBan phieu = phieuDatBanRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        // Dọn sạch bàn cũ, gán bàn mới (Luồng cập nhật đổi 1 lấy 1)
        phieu.getBanAns().clear();
        phieu.getBanAns().add(banAnMoi);
        phieuDatBanRepository.save(phieu);
    }

    @Transactional
    public void updateCheckIn(DatBanUpdateRequest request) {
        // =========================================================================
        // 1. XỬ LÝ NGHIỆP VỤ ĐỔI BÀN (NẾU CÓ idBanAnMoi)
        // =========================================================================
        if (request.getId() != null && request.getIdBanAnMoi() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            BanAn banCu = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn cũ"));
            BanAn banMoi = banAnRepository.findById(request.getIdBanAnMoi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn mới"));

            String maBanCu = banCu.getMaBan();
            String maBanMoi = banMoi.getMaBan();

            // 🚨 LOGIC N-N: Rút bàn cũ ra khỏi Phiếu, nhét bàn mới vào
            phieu.getBanAns().remove(banCu);
            phieu.getBanAns().add(banMoi);

            banCu.setTrangThai(0);
            banMoi.setTrangThai(1);

            banAnRepository.save(banCu);
            banAnRepository.save(banMoi);
            phieuDatBanRepository.save(phieu);

            if (hoaDon != null) {
                ghiLichSu(hoaDon, request.getIdNhanVien(),
                        "Đổi bàn: " + maBanCu + " -> " + maBanMoi,
                        "Chuyển một phần/toàn bộ khách sang bàn mới",
                        hoaDon.getTrangThaiHoaDon(), hoaDon.getTrangThaiHoaDon());
            }
            return;
        }

        // =========================================================================
        // 2. 🚨 LOGIC MỞ BÀN PHỤ / KHÁCH VÃNG LAI (KHI id BỊ NULL)
        // =========================================================================
        if (request.getId() == null && request.getTrangThai() != null && request.getTrangThai() == 1) {

            // 🚨 BƯỚC FIX CỐT LÕI: Kiểm tra xem bàn này có đang phục vụ ai không?
            // Sử dụng phương thức findActiveBillByBanAn chúng ta đã sửa trong Repository
            Optional<HoaDonThanhToan> existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(request.getIdBanAn());

            if (existingBill.isPresent()) {
                throw new RuntimeException("Bàn này hiện đang có khách (HĐ #" + existingBill.get().getMaHoaDon() + "), không thể mở phiếu mới!");
            }

            BanAn banPhu = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

            banPhu.setTrangThai(1);
            banAnRepository.save(banPhu);

            PhieuDatBan phieuMoi = new PhieuDatBan();
            // 🚨 Dùng helper setIdBanAn để dọn dẹp quan hệ N-N
            phieuMoi.setIdBanAn(banPhu);
            phieuMoi.setThoiGianDat(java.time.LocalDateTime.now());
            phieuMoi.setSoLuongKhach(banPhu.getSoNguoiToiDa());
            phieuMoi.setTrangThai(3); // Đang sử dụng
            phieuMoi.setHinhThucDat(2); // Vãng lai
            phieuMoi.setNguoiTao("POS System");

            if (request.getMaDatBanGoc() != null) {
                phieuMoi.setMaDatBan(request.getMaDatBanGoc());
            }

            KhachHang kh = null;
            if (request.getIdKhachHang() != null) {
                kh = khachHangRepository.findById(request.getIdKhachHang()).orElse(null);
                phieuMoi.setIdKhachHang(kh);
            }
            phieuMoi = phieuDatBanRepository.save(phieuMoi);

            HoaDonThanhToan hoaDonMoi = new HoaDonThanhToan();
            hoaDonMoi.setIdPhieuDatBan(phieuMoi);
            if (kh != null) hoaDonMoi.setIdKhachHang(kh);

            hoaDonMoi.setThoiGianTao(Instant.now());
            hoaDonMoi.setTrangThaiHoaDon(4); // 4: Đang phục vụ
            hoaDonMoi.setTongTienChuaGiam(java.math.BigDecimal.ZERO);
            hoaDonMoi.setTongTienThanhToan(java.math.BigDecimal.ZERO);
            hoaDonMoi.setVatApDung(10.0f);

            if (request.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(request.getIdNhanVien()).orElse(null);
                hoaDonMoi.setIdNhanVien(nv);
            }

            hoaDonMoi = hoaDonThanhToanRepository.save(hoaDonMoi);
            ghiLichSu(hoaDonMoi, request.getIdNhanVien(), "Mở bàn vãng lai", "Tạo hóa đơn trực tiếp", 4, 4);
            return;
        }

        // =========================================================================
        // 3. CẬP NHẬT TRẠNG THÁI BÀN ĂN (🚨 ĐÃ SỬA ĐỂ DỌN SẠCH CẢ ĐOÀN)
        // =========================================================================
        if (request.getTrangThai() != null && request.getTrangThai() == 0 && request.getId() != null) {
            // Nếu có lệnh dọn bàn (trạng thái = 0) và có ID Phiếu -> Dọn sạch toàn bộ bàn
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId()).orElse(null);
            if (phieu != null) {
                for (BanAn ban : phieu.getBanAns()) {
                    ban.setTrangThai(0);
                    banAnRepository.save(ban);
                }
            }
        } else if (request.getIdBanAn() != null && request.getTrangThai() != null) {
            // Luồng cập nhật 1 bàn lẻ tẻ (VD: giữ bàn lúc VNPay)
            BanAn banAn = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));
            banAn.setTrangThai(request.getTrangThai());
            banAnRepository.save(banAn);
        }

        // =========================================================================
        // 4. CẬP NHẬT TRẠNG THÁI PHIẾU ĐẶT BÀN
        // =========================================================================
        if (request.getId() != null && request.getTrangThaiPhieu() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));
            phieu.setTrangThai(request.getTrangThaiPhieu());
            phieuDatBanRepository.save(phieu);
        }

        // =========================================================================
        // 5. CẬP NHẬT ĐỒNG BỘ TRẠNG THÁI HÓA ĐƠN & GHI LOG
        // =========================================================================
        if (request.getId() != null) {
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId()).orElse(null);

            if (hoaDon != null && phieu != null) {
                Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();
                Integer trangThaiMoi = trangThaiCu;
                String hanhDongLog = "";
                String lyDoLog = "Cập nhật hệ thống";

                if (request.getTrangThaiHoaDon() != null) {
                    trangThaiMoi = request.getTrangThaiHoaDon();
                    hoaDon.setTrangThaiHoaDon(trangThaiMoi);

                    if (trangThaiMoi == 5) {
                        hanhDongLog = "Yêu cầu tính tiền";
                        lyDoLog = "Khách yêu cầu hóa đơn tạm tính";
                    } else if (trangThaiMoi == 6) {
                        hanhDongLog = "Đã thanh toán";
                        lyDoLog = "Hoàn tất thanh toán tại quầy";
                        hoaDon.setThoiGianThanhToan(Instant.now().plus(7, ChronoUnit.HOURS));
                        phieu.setTrangThai(4); // Phiếu Hoàn Thành

                        // 🚨 BẢO HIỂM THÊM 1 LỚP: Khi thanh toán tiền mặt xong, chắc chắn dọn hết bàn
                        for (BanAn ban : phieu.getBanAns()) {
                            ban.setTrangThai(0);
                            banAnRepository.save(ban);
                        }
                    } else if (trangThaiMoi == 8) {
                        hanhDongLog = "Hủy hóa đơn";
                        lyDoLog = "Hủy phiếu đặt bàn / Hủy hóa đơn";
                        phieu.setTrangThai(2); // Phiếu Hủy

                        // 🚨 BẢO HIỂM LỚP 2: Khi Hủy phiếu, chắc chắn dọn hết bàn
                        for (BanAn ban : phieu.getBanAns()) {
                            ban.setTrangThai(0);
                            banAnRepository.save(ban);
                        }
                    }
                    phieuDatBanRepository.save(phieu);

                } else if (request.getTrangThaiPhieu() != null) {
                    if (request.getTrangThaiPhieu() == 3 && trangThaiCu < 4) {
                        trangThaiMoi = 4; // Trạng thái Phục vụ của Hóa Đơn
                        hoaDon.setTrangThaiHoaDon(trangThaiMoi);
                        hanhDongLog = "Khách nhận bàn";
                        lyDoLog = "Nhân viên xác nhận khách đã vào bàn";
                    }
                }

                if (!hanhDongLog.isEmpty()) {
                    ghiLichSu(hoaDon, request.getIdNhanVien(), hanhDongLog, lyDoLog, trangThaiCu, trangThaiMoi);
                }

                if (request.getTienMat() != null && request.getTienMat().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal tienHienTai = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : BigDecimal.ZERO;
                    hoaDon.setTienKhachDua(tienHienTai.add(request.getTienMat()));

                    ghiLichSu(hoaDon, request.getIdNhanVien(),
                            "Thu tiền mặt: " + request.getTienMat().setScale(0) + "đ",
                            "Thanh toán tại quầy", trangThaiMoi, trangThaiMoi);

                    saveLichSuThanhToan(hoaDon, request.getTienMat(), "PT02", "CASH_");
                }

                hoaDonThanhToanRepository.save(hoaDon);
            }
        }
    }

    private void saveLichSuThanhToan(HoaDonThanhToan hd, BigDecimal soTien, String maPT, String prefixMaGD) {
        try {
            PhuongThucThanhToan pt = phuongThucThanhToanRepository.findByMaPhuongThuc(maPT);
            LichSuThanhToan ls = new LichSuThanhToan();
            ls.setPhuongThucThanhToan(pt);
            ls.setHoaDon(hd);
            ls.setTenPhuongThuc(pt != null ? pt.getTenPhuongThuc() : maPT);
            ls.setMaGiaoDich(prefixMaGD.contains("_") ? prefixMaGD + System.currentTimeMillis() : prefixMaGD);
            ls.setSoTienThanhToan(soTien);
            ls.setLoaiGiaoDich(1);
            ls.setNgayThanhToan(Instant.now());
            ls.setTrangThai(1);
            lichSuThanhToanRepository.save(ls);
        } catch (Exception e) {
            System.out.println("Lỗi lưu lịch sử thanh toán: " + e.getMessage());
        }
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

    @Transactional
    public void autoUpdateTrangThaiPhieu() {
        int quaHan = phieuDatBanRepository.updateChoXacNhanQuaHan();
        int daHuy = phieuDatBanRepository.updateDaXacNhanQuaGio();
        System.out.println("Auto update: " + quaHan + " QUÁ HẠN, " + daHuy + " ĐÃ HỦY");
    }

    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        phieuDatBanRepository.updateTrangThai(id, trangThai);
    }

    public List<BanTrangThaiResponse> getTrangThaiBanTheoNgay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<PhieuDatBan> phieuList = phieuDatBanRepository.findPhieuTrongNgay(start, end);
        Map<Integer, Integer> banTrangThaiMap = new HashMap<>();

        // 🚨 LOGIC N-N: Quét từng bàn trong mỗi phiếu
        for (PhieuDatBan phieu : phieuList) {
            Integer trangThaiPhieu = phieu.getTrangThai();

            for (BanAn ban : phieu.getBanAns()) {
                Integer banId = ban.getId();

                if (trangThaiPhieu == 3) {
                    banTrangThaiMap.put(banId, 1);
                    continue;
                }

                if (!banTrangThaiMap.containsKey(banId)) {
                    if (trangThaiPhieu == 0 || trangThaiPhieu == 1) {
                        banTrangThaiMap.put(banId, 2);
                    }
                }
            }
        }

        return banTrangThaiMap.entrySet()
                .stream()
                .map(e -> new BanTrangThaiResponse(e.getKey(), e.getValue()))
                .toList();
    }


    @Transactional
    public void createFull(CreateBanFullRequest req) {

        KhuVuc kv = khuVucRepository
                .findByTenKhuVucAndTang(req.getTenKhuVuc(), req.getTang())
                .orElse(null);

        if (kv == null) {
            kv = new KhuVuc();
            kv.setTenKhuVuc(req.getTenKhuVuc());
            kv.setTang(req.getTang());
            kv.setMaKhuVuc("TEMP");
            kv = khuVucRepository.save(kv);

            String ma = "KV" + String.format("%03d", kv.getId());
            kv.setMaKhuVuc(ma);
            khuVucRepository.save(kv);
        }

        BanAn ban = new BanAn();
        ban.setIdKhuVuc(kv);
        ban.setSoNguoiToiDa(req.getSoNguoiToiDa());
        ban.setLoaiDatBan(req.getLoaiDatBan());
        ban.setTrangThai(0);
        ban.setTenBan("SC" + ban.getId());
        banAnRepository.save(ban);
    }

    public KhuVuc create(KhuVucRequest request) {

        if (request.getTang() == null || request.getTang() <= 0) {
            throw new RuntimeException("Tầng không hợp lệ");
        }

        if (khuVucRepository.existsByTangAndTenKhuVucAndTrangThai(
                request.getTang(),
                request.getTenKhuVuc().trim(),
                1)) {
            throw new RuntimeException("Khu vực đã tồn tại");
        }

        KhuVuc kv = new KhuVuc();
        kv.setTang(request.getTang());
        kv.setTenKhuVuc(request.getTenKhuVuc().trim());
        kv.setMoTa(request.getMoTa());
        kv.setTrangThai(1);

        return khuVucRepository.save(kv);
    }

    public List<KhuVuc> getAllActive() {
        return khuVucRepository.findByTrangThai(1);
    }

    public List<KhuVuc> getByTang(Integer tang) {
        return khuVucRepository.findByTangAndTrangThai(tang, 1);
    }

    public void delete(Integer id) {
        KhuVuc kv = khuVucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        kv.setTrangThai(0);
        khuVucRepository.save(kv);
    }

    public List<BanAn> timDanhSachBanTrong(DatBanRequest request) {
        // 1. Kiểm tra xem DB có setup LOẠI BÀN nào đủ sức chứa không
        List<BanAn> danhSachBanPhuHop = banAnRepository.findBanPhuHopChoDatBan(request.getSoNguoi());
        if (danhSachBanPhuHop.isEmpty()) {
            return new java.util.ArrayList<>(); // Đội quá đông, không có bàn/ghép bàn phù hợp
        }

        LocalDateTime thoiGianKhachDen = LocalDateTime.of(request.getNgayDat(), request.getGioDat());
        // Khung giờ +/- 2 tiếng (thời gian ăn trung bình)
        LocalDateTime start = thoiGianKhachDen.minusHours(2);
        LocalDateTime end = thoiGianKhachDen.plusHours(2);

        // 2. Lấy thông tin thống kê thực tế
        List<BanAn> tatCaBanQuan = banAnRepository.findAll();
        List<PhieuDatBan> phieuCungGio = phieuDatBanRepository.findPhieuOverlapping(start, end);

        // 3. Tính toán Quota
        int tongSoBan = tatCaBanQuan.size();
        int tongSucChua = tatCaBanQuan.stream().mapToInt(BanAn::getSoNguoiToiDa).sum();

        int soPhieuDaDat = phieuCungGio.size();
        int soKhachDaDat = phieuCungGio.stream().mapToInt(PhieuDatBan::getSoLuongKhach).sum();

        // 4. KIỂM TRA ĐIỀU KIỆN (GÁC CỔNG)

        // Điều kiện A: Số lượng đoàn khách không được vượt quá số lượng bàn vật lý
        if (soPhieuDaDat >= tongSoBan) {
            return new java.util.ArrayList<>(); // Hết bàn
        }

        // Điều kiện B: Tổng khách không được vượt quá 90% sức chứa của quán
        // (Chừa lại 10% rủi ro phân mảnh bàn và khách Walk-in vãng lai)
        int maxCapacity = (int) (tongSucChua * 0.9);
        if ((soKhachDaDat + request.getSoNguoi()) > maxCapacity) {
            return new java.util.ArrayList<>(); // Rủi ro hết chỗ ngồi liền kề -> Chặn
        }

        // 5. Nếu vượt qua gác cổng -> Quán an toàn để nhận đơn.
        return danhSachBanPhuHop;
    }

    public List<BanAnResponse> checkBanTrong(DatBanRequest request) {
        List<BanAn> banTrongEntityList = timDanhSachBanTrong(request);
        return banTrongEntityList.stream()
                .map(BanAnResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackOn = Exception.class)
    public Map<String, Object> taoPhieuDatBanOnline(DatBanOnlineRequest request) {
        // 1. Xử lý khách hàng (Đã tối ưu để tương thích với Google Login)
        KhachHang khachHang = null;

        // Ưu tiên 1: Lấy ID từ tài khoản đang đăng nhập (Bao gồm cả Google)
        if (request.getIdKhachHang() != null) {
            khachHang = khachHangRepository.findById(request.getIdKhachHang()).orElse(null);
            // Nếu là khách Google (chưa có SĐT), thì cập nhật SĐT từ form vào luôn
            if (khachHang != null && (khachHang.getSoDienThoai() == null || khachHang.getSoDienThoai().isEmpty())) {
                khachHang.setSoDienThoai(request.getPhone());
                khachHang = khachHangRepository.save(khachHang);
            }
        }

        // Ưu tiên 2: Khách vãng lai -> Tìm bằng SĐT
        if (khachHang == null) {
            khachHang = khachHangRepository.findBySoDienThoai(request.getPhone()).orElse(null);
        }

        // Ưu tiên 3: Nếu vẫn không có -> Tạo khách mới
        if (khachHang == null) {
            khachHang = new KhachHang();
            khachHang.setTenKhachHang(request.getFullName());
            khachHang.setSoDienThoai(request.getPhone());
            khachHang.setEmail(request.getEmail());
            khachHang.setTenDangNhap(request.getPhone());
            khachHang.setMatKhauDangNhap(request.getPhone());
            khachHang.setTrangThai(1);
            khachHang.setAuthProvider(com.example.datn_cozypot_spring_boot.config.AuthProvider.LOCAL);
            khachHang.setNgayTaoTaiKhoan(java.time.Instant.now());
            khachHang = khachHangRepository.save(khachHang);
        }

        PhieuDatBan phieu = new PhieuDatBan();
        phieu.setIdKhachHang(khachHang);
        phieu.setThoiGianDat(request.getThoiGianDat());
        phieu.setHinhThucDat(1);
        phieu.setSoLuongKhach(request.getSoNguoi());
        phieu.setTrangThai(0);
        phieu.setNguoiTao("Khách hàng");
        phieu.setNgayTao(java.time.LocalDateTime.now());
        phieu = phieuDatBanRepository.save(phieu);

        HoaDonThanhToan hoaDon = new HoaDonThanhToan();
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdPhieuDatBan(phieu);

        String thongTinKhachNhap = String.format("[Hệ thống ghi nhận] Khách: %s | SĐT: %s | Email: %s. ",
                request.getFullName(), request.getPhone(), request.getEmail() != null ? request.getEmail() : "Không có");
        String ghiChuGoc = request.getGhiChu() != null ? "Ghi chú của khách: " + request.getGhiChu() : "";

        hoaDon.setGhiChu(thongTinKhachNhap + ghiChuGoc);
        hoaDon.setThoiGianTao(Instant.now());

        BigDecimal tongTien = request.getTongTien() != null ? request.getTongTien() : BigDecimal.ZERO;
        BigDecimal tienCoc = request.getTienCoc() != null ? request.getTienCoc() : BigDecimal.ZERO;

        hoaDon.setTongTienChuaGiam(tongTien);
        hoaDon.setTongTienThanhToan(tongTien);
        hoaDon.setTienCoc(tienCoc);
        hoaDon.setSoTienDaGiam(BigDecimal.ZERO);

        int trangThaiBanDau = (tienCoc.compareTo(BigDecimal.ZERO) > 0) ? 1 : 0;
        hoaDon.setTrangThaiHoaDon(trangThaiBanDau);

        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        List<LichSuHoaDon> dsLichSu = new ArrayList<>();

        LichSuHoaDon logTaoMoi = new LichSuHoaDon();
        logTaoMoi.setIdHoaDon(hoaDon);
        logTaoMoi.setHanhDong("Tạo hóa đơn online");
        logTaoMoi.setLyDoThucHien(String.format("Khách %s (%s) tạo đơn đặt bàn qua Website", request.getFullName(), request.getPhone()));
        logTaoMoi.setTrangThaiTruocDo(null);
        logTaoMoi.setTrangThaiMoi(0);
        logTaoMoi.setThoiGianThucHien(Instant.now());
        dsLichSu.add(logTaoMoi);

        // 5. Lưu Chi Tiết Món Ăn (Đã tối ưu dùng saveAll)
        if (request.getChiTiet() != null && !request.getChiTiet().isEmpty()) {
            List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

            for (PhieuDatBanRequest.ChiTietMonAnRequest mon : request.getChiTiet()) {
                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setIdHoaDon(hoaDon);

                String tenMon = "Món không xác định";

                if (mon.getIdChiTietMonAn() != null) {
                    DanhMucChiTiet dmct = danhMucChiTietRepository.findById(mon.getIdChiTietMonAn()).orElse(null);
                    if (dmct != null) {
                        tenMon = dmct.getTenMon();
                        ct.setIdChiTietMonAn(dmct);
                    }
                } else if (mon.getIdSetLau() != null) {
                    SetLau sl = setLauRepository.findById(mon.getIdSetLau()).orElse(null);
                    if (sl != null) {
                        tenMon = sl.getTenSetLau();
                        ct.setIdSetLau(sl);
                    }
                }

                ct.setSoLuong(mon.getSoLuong());
                ct.setDonGiaTaiThoiDiemBan(mon.getDonGia());
                ct.setThanhTien(mon.getDonGia().multiply(new BigDecimal(mon.getSoLuong())));
                ct.setTrangThaiMon(1);
                ct.setNgayGioTao(java.time.LocalDateTime.now());

                dsChiTiet.add(ct);

                // Tạo log cho từng món
                LichSuHoaDon logDatMon = new LichSuHoaDon();
                logDatMon.setIdHoaDon(hoaDon);
                logDatMon.setHanhDong("Thêm món ăn");
                logDatMon.setLyDoThucHien(String.format("Khách đặt trước: %dx %s", mon.getSoLuong(), tenMon));
                logDatMon.setTrangThaiTruocDo(0);
                logDatMon.setTrangThaiMoi(0);
                logDatMon.setThoiGianThucHien(Instant.now());
                dsLichSu.add(logDatMon);
            }

            // Lưu toàn bộ chi tiết món ăn trong 1 lần
            chiTietHoaDonRepository.saveAll(dsChiTiet);

            // Ép Hibernate xả dữ liệu để Trigger tính tổng tiền trong Database chạy
            chiTietHoaDonRepository.flush();
        }

        // 6. Ghi log Lịch Sử Hóa Đơn cọc
        if (trangThaiBanDau == 1) {
            LichSuHoaDon logChoCoc = new LichSuHoaDon();
            logChoCoc.setIdHoaDon(hoaDon);
            logChoCoc.setHanhDong("Yêu cầu thanh toán cọc");
            logChoCoc.setLyDoThucHien("Hệ thống yêu cầu cọc để giữ bàn");
            logChoCoc.setTrangThaiTruocDo(0);
            logChoCoc.setTrangThaiMoi(1);
            logChoCoc.setThoiGianThucHien(Instant.now());
            dsLichSu.add(logChoCoc);
        }

        lichSuHoaDonRepository.saveAll(dsLichSu);

        HoaDonThanhToan hoaDonMoiNhat = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElse(hoaDon);

        if (trangThaiBanDau == 0 && request.getEmail() != null && !request.getEmail().isBlank()) {
            String maTraCuu = "PDB" + String.format("%04d", phieu.getId());
            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(request.getFullName())
                    .soDienThoai(request.getPhone())
                    .email(request.getEmail())
                    .thoiGianDat(request.getThoiGianDat() != null ?
                            request.getThoiGianDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Chưa xác định")
                    .soLuongKhach(request.getSoNguoi())
                    .maPhieuDatBan(maTraCuu)
                    .build();

            // Gọi hàm Async, luồng chính vẫn tiếp tục chạy nhanh chóng
            emailDatBanService.sendEmailCamOnDatBan(emailDto);
        }

        // 8. Trả kết quả về cho Controller
        Map<String, Object> result = new HashMap<>();
        result.put("idHoaDon", hoaDonMoiNhat.getId());
        result.put("tienCoc", hoaDonMoiNhat.getTienCoc());
        result.put("trangThaiHoaDon", hoaDonMoiNhat.getTrangThaiHoaDon());

        return result;
    }

    @Transactional
    public CreatePhieuDatBanFullResponse createFull(CreatePhieuDatBanFullRequest req) {

        KhachHang khachHang;

        if (req.getIdKhachHang() != null) {
            khachHang = khachHangRepository.findById(req.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        } else {
            if (req.getSoDienThoai() == null || req.getSoDienThoai().isBlank()) {
                throw new RuntimeException("Số điện thoại không được để trống");
            }
            Optional<KhachHang> existing = khachHangRepository.findBySoDienThoai(req.getSoDienThoai());
            if (existing.isPresent()) {
                khachHang = existing.get();
            } else {
                khachHang = new KhachHang();
                khachHang.setTenKhachHang(req.getTenKhachHang());
                khachHang.setSoDienThoai(req.getSoDienThoai());
                khachHang.setEmail(req.getEmail());
                khachHang.setNgaySinh(req.getNgaySinh());
                khachHang.setGioiTinh(req.getGioiTinh());
                khachHang.setTrangThai(1);
                khachHang = khachHangRepository.save(khachHang);
            }
        }

        BanAn ban = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        LocalDateTime start = req.getThoiGianDat().minusHours(2);
        LocalDateTime end   = req.getThoiGianDat().plusHours(2);

        boolean isTrung = phieuDatBanRepository.existsByTimeRange(ban, start, end);

        if (isTrung) {
            throw new RuntimeException("Bàn đã có lịch trong vòng 2 tiếng");
        }

        // 1. TẠO PHIẾU ĐẶT BÀN
        PhieuDatBan pdb = new PhieuDatBan();
        pdb.getBanAns().add(ban); // LOGIC N-N
        pdb.setIdKhachHang(khachHang);
        pdb.setThoiGianDat(req.getThoiGianDat());
        pdb.setHinhThucDat(req.getHinhThucDat());
        pdb.setSoLuongKhach(req.getSoLuongKhach());
        pdb.setTrangThai(0);
        PhieuDatBan saved = phieuDatBanRepository.save(pdb);

        ban.setTrangThai(0);
        banAnRepository.save(ban);

        // =======================================================
        // 2. TẠO HÓA ĐƠN THANH TOÁN (KÈM LẤY VAT TỪ DB)
        // =======================================================
        HoaDonThanhToan hd = new HoaDonThanhToan();
        hd.setIdPhieuDatBan(saved);
        hd.setIdKhachHang(khachHang);
        hd.setThoiGianTao(Instant.now());
        hd.setTrangThaiHoaDon(0); // 0: Mới khởi tạo / Chờ cọc

        // Set mặc định các trường tiền tệ
        hd.setTongTienChuaGiam(BigDecimal.ZERO);
        hd.setSoTienDaGiam(BigDecimal.ZERO);
        hd.setTienCoc(BigDecimal.ZERO);
        hd.setTienHoanTra(BigDecimal.ZERO);
        hd.setTongTienThanhToan(BigDecimal.ZERO);
        hd.setTienKhachDua(BigDecimal.ZERO);
        hd.setTienThua(BigDecimal.ZERO);
        hd.setDiemSuDung(0);
        hd.setDiemCongThem(0);

        // Lấy VAT từ bảng Tham Số Hệ Thống
        Float vatApDung = 10.0F; // Dự phòng nếu lỗi
        try {
            Optional<ThamSoHeThong> optVat = thamSoHeThongRepository.findByMaThamSo("VAT");
            if (optVat.isPresent()) {
                vatApDung = Float.parseFloat(optVat.get().getGiaTri());
            }
        } catch (Exception e) {
            log.warn("Không thể lấy VAT từ database, sử dụng mặc định 10%");
        }
        hd.setVatApDung(vatApDung);

        // Lưu hóa đơn
        hd = hoaDonThanhToanRepository.save(hd);

        // 3. GHI LOG LỊCH SỬ HÓA ĐƠN
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setIdHoaDon(hd);
        ls.setHanhDong("Tạo phiếu đặt bàn");
        ls.setLyDoThucHien("Khách đặt trước bàn " + ban.getMaBan());
        ls.setThoiGianThucHien(Instant.now());
        ls.setTrangThaiTruocDo(null);
        ls.setTrangThaiMoi(0);
        lichSuHoaDonRepository.save(ls);
        // =======================================================


        // 4. XỬ LÝ GỬI EMAIL
        boolean daGuiMail = false;
        if (khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {
            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .soDienThoai(khachHang.getSoDienThoai())
                    .email(khachHang.getEmail())
                    .thoiGianDat(req.getThoiGianDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .tenBan(ban.getMaBan())
                    .khuVuc(ban.getIdKhuVuc() != null ? ban.getIdKhuVuc().getTenKhuVuc() + " - Tầng " + ban.getIdKhuVuc().getTang() : "N/A")
                    .soLuongKhach(req.getSoLuongKhach())
                    .maPhieuDatBan("PDB-" + saved.getId())
                    .build();

            try {
                emailDatBanService.sendXacNhanDatBanSync(emailDto);
                saved.setTrangThai(1);
                phieuDatBanRepository.save(saved);

                // Cập nhật trạng thái Hóa đơn sang 1 (Chờ xác nhận)
                hd.setTrangThaiHoaDon(1);
                hoaDonThanhToanRepository.save(hd);

                daGuiMail = true;
            } catch (Exception e) {
                log.warn("⚠️ Gửi mail thất bại, phiếu PDB-{} giữ trạng thái chờ xác nhận: {}", saved.getId(), e.getMessage());
            }
        }

        return CreatePhieuDatBanFullResponse.builder()
                .idPhieuDatBan(saved.getId())
                .maPhieuDatBan("PDB-" + saved.getId())
                .daGuiMail(daGuiMail)
                .emailGuiToi(daGuiMail ? khachHang.getEmail() : null)
                .build();
    }

    public List<KhachHangSelectDTO> getAllForSelect() {
        return khachHangRepository.findAll().stream().map(this::toSelectDTO).collect(Collectors.toList());
    }

    public List<KhachHangSelectDTO> searchByKeyword(String keyword) {
        return khachHangRepository.searchByKeyword(keyword).stream().map(this::toSelectDTO).collect(Collectors.toList());
    }

    public KhachHangSelectDTO findBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai).map(this::toSelectDTO).orElse(null);
    }

    private KhachHangSelectDTO toSelectDTO(KhachHang k) {
        return KhachHangSelectDTO.builder()
                .idKhachHang(k.getId())
                .maKhachHang(k.getMaKhachHang())
                .tenKhachHang(k.getTenKhachHang())
                .soDienThoai(k.getSoDienThoai())
                .email(k.getEmail())
                .ngaySinh(k.getNgaySinh())
                .gioiTinh(k.getGioiTinh())
                .build();
    }

    @Transactional
    public void xacNhanVaGuiMail(Integer idHoaDon, Integer idNhanVien) {
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
        if (phieu == null) {
            throw new RuntimeException("Hóa đơn này không có phiếu đặt bàn liên kết!");
        }

        if (phieu.getBanAns() == null || phieu.getBanAns().isEmpty()) {
            throw new RuntimeException("Vui lòng xếp bàn cho khách trước khi Xác nhận và Gửi mail!");
        }

        if (hoaDon.getTrangThaiHoaDon() != 0 && hoaDon.getTrangThaiHoaDon() != 2) {
            throw new RuntimeException("Trạng thái hóa đơn không hợp lệ để xác nhận!");
        }

        Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

        hoaDon.setTrangThaiHoaDon(3);
        phieu.setTrangThai(1);

        hoaDonThanhToanRepository.save(hoaDon);
        phieuDatBanRepository.save(phieu);

        ghiLichSu(hoaDon, idNhanVien, "Xác nhận & Gửi Mail", "Nhân viên xác nhận đơn đặt bàn và gửi email cho khách", trangThaiCu, 3);

        KhachHang khachHang = hoaDon.getIdKhachHang();

        String danhSachTenBan = phieu.getBanAns().stream()
                .map(BanAn::getMaBan)
                .collect(Collectors.joining(", "));

        BanAn banDaiDien = phieu.getBanAns().iterator().next();
        String tenKhuVuc = (banDaiDien.getIdKhuVuc() != null)
                ? banDaiDien.getIdKhuVuc().getTenKhuVuc() + " - Tầng " + banDaiDien.getIdKhuVuc().getTang()
                : "Chưa xác định";

        if (khachHang != null && khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {
            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .soDienThoai(khachHang.getSoDienThoai())
                    .email(khachHang.getEmail())
                    .thoiGianDat(phieu.getThoiGianDat() != null ? phieu.getThoiGianDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Chưa xác định")
                    .tenBan(danhSachTenBan) // Gắn danh sách bàn vào mail
                    .khuVuc(tenKhuVuc)
                    .soLuongKhach(phieu.getSoLuongKhach())
                    .maPhieuDatBan(phieu.getMaDatBan() != null ? phieu.getMaDatBan() : "PDB-" + phieu.getId())
                    .build();

            try {
                emailDatBanService.sendXacNhanDatBanSync(emailDto);
                log.info("✅ Đã gửi mail xác nhận thành công cho hóa đơn: {}", idHoaDon);
            } catch (Exception e) {
                log.error("⚠️ Lỗi gửi mail cho hóa đơn {}: {}", idHoaDon, e.getMessage());
                throw new RuntimeException("Xác nhận thành công nhưng hệ thống gửi mail gặp sự cố. Vui lòng kiểm tra lại cấu hình Email!");
            }
        } else {
            throw new RuntimeException("Xác nhận thành công nhưng Khách hàng chưa cung cấp địa chỉ Email để gửi thông báo!");
        }
    }
}