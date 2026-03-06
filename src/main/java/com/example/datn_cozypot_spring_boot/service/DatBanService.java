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
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

        // Lấy danh sách các phiếu đặt bàn "ảnh hưởng đến hiện tại" (logic chúng ta vừa sửa ở trên)
        List<DatBanListResponse> waitingToday = getAllByTrangThai();

        return allBan.stream().map(ban -> {
            BanAnResponse res = new BanAnResponse(ban);

            // 1. Kiểm tra xem bàn này có nằm trong danh sách "Sắp có khách đến" không
            boolean isReservedToday = waitingToday.stream()
                    .anyMatch(p -> p.getMaBan().equals(ban.getMaBan()));

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
        banAn.setIdKhuVuc(khuVuc);          // ⭐ DÒNG QUYẾT ĐỊNH
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




    public Page<DatBanListResponse> searchDatBan(
            DatBanSearchRequest request,
            Pageable pageable
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (request.getThoiGianDat() != null) {
            LocalDate date = request.getThoiGianDat();
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        }


        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.unsorted()   // QUAN TRỌNG
        );

        Page<PhieuDatBan> page = phieuDatBanRepository.search(
                request.getSoDienThoai(),
                request.getTrangThai(),
                start,
                end,
                pageRequest
        );

        return page.map(DatBanListResponse::new);
    }


    @Transactional
    public void updateBanChoPhieu(DatBanUpdateRequest req) {

        BanAn banAn = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        phieuDatBanRepository.updateBanChoPhieu(
                req.getId(),
                banAn
        );
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

            BanAn banCu = phieu.getIdBanAn();
            BanAn banMoi = banAnRepository.findById(request.getIdBanAnMoi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn mới"));

            String maBanCu = banCu.getMaBan();
            String maBanMoi = banMoi.getMaBan();

            phieu.setIdBanAn(banMoi);
            if (hoaDon != null) {
                hoaDon.setIdBanAn(banMoi);
            }

            banCu.setTrangThai(0);
            banMoi.setTrangThai(1);

            banAnRepository.save(banCu);
            banAnRepository.save(banMoi);
            phieuDatBanRepository.save(phieu);

            if (hoaDon != null) {
                ghiLichSu(hoaDon, request.getIdNhanVien(),
                        "Đổi bàn: " + maBanCu + " -> " + maBanMoi,
                        "Chuyển toàn bộ dữ liệu từ bàn cũ sang bàn mới",
                        hoaDon.getTrangThaiHoaDon(), hoaDon.getTrangThaiHoaDon());
                hoaDonThanhToanRepository.save(hoaDon);
            }
            return;
        }

        // =========================================================================
        // 2. 🚨 LOGIC TÁCH BÀN / MỞ BÀN PHỤ / KHÁCH VÃNG LAI (KHI id BỊ NULL)
        // =========================================================================
        if (request.getId() == null && request.getTrangThai() != null && request.getTrangThai() == 1) {
            BanAn banPhu = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

            banPhu.setTrangThai(1);
            banAnRepository.save(banPhu);

            PhieuDatBan phieuMoi = new PhieuDatBan();
            phieuMoi.setIdBanAn(banPhu);
            phieuMoi.setThoiGianDat(java.time.LocalDateTime.now());
            phieuMoi.setSoLuongKhach(banPhu.getSoNguoiToiDa());
            phieuMoi.setTrangThai(3); // 🚨 Đã check-in / Đang sử dụng
            phieuMoi.setHinhThucDat(2);
            phieuMoi.setNguoiTao("Hệ thống");

            if (request.getMaDatBanGoc() != null) {
                phieuMoi.setMaDatBan(request.getMaDatBanGoc());
            }

            // GÁN ID KHÁCH HÀNG CHO PHIẾU
            KhachHang kh = null;
            if (request.getIdKhachHang() != null) {
                kh = khachHangRepository.findById(request.getIdKhachHang()).orElse(null);
                phieuMoi.setIdKhachHang(kh);
            }
            phieuMoi = phieuDatBanRepository.save(phieuMoi);

            // TẠO HÓA ĐƠN CHO BÀN PHỤ
            HoaDonThanhToan hoaDonMoi = new HoaDonThanhToan();
            hoaDonMoi.setIdPhieuDatBan(phieuMoi);
            hoaDonMoi.setIdBanAn(banPhu);

            if (kh != null) {
                hoaDonMoi.setIdKhachHang(kh);
            }

            hoaDonMoi.setThoiGianTao(Instant.now());
            hoaDonMoi.setTrangThaiHoaDon(4);
            hoaDonMoi.setTongTienChuaGiam(java.math.BigDecimal.ZERO);
            hoaDonMoi.setSoTienDaGiam(java.math.BigDecimal.ZERO);
            hoaDonMoi.setTienCoc(java.math.BigDecimal.ZERO);
            hoaDonMoi.setTongTienThanhToan(java.math.BigDecimal.ZERO);

            Double vatFromRequest = request.getVatApDung();
            float finalVat = (vatFromRequest != null) ? vatFromRequest.floatValue() : 10.0f;
            hoaDonMoi.setVatApDung(finalVat);

            if (request.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(request.getIdNhanVien()).orElse(null);
                hoaDonMoi.setIdNhanVien(nv);
            }

            hoaDonMoi = hoaDonThanhToanRepository.save(hoaDonMoi);

            ghiLichSu(hoaDonMoi, request.getIdNhanVien(), "Mở bàn phụ", "Tạo hóa đơn cho bàn tách", 4, 4);
            return;
        }

        // =========================================================================
        // 3. CẬP NHẬT TRẠNG THÁI BÀN ĂN (CHO LUỒNG CŨ CÓ ID PHIẾU)
        // =========================================================================
        if (request.getIdBanAn() != null) {
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
        // 5. 🚨 CẬP NHẬT ĐỒNG BỘ TRẠNG THÁI HÓA ĐƠN & GHI LOG
        // =========================================================================
        if (request.getId() != null) {
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId()).orElse(null); // Lấy phiếu ra để đồng bộ

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

                        // 🚨 ĐỒNG BỘ PHIẾU: Hóa đơn thanh toán xong -> Phiếu Hoàn thành (4)
                        phieu.setTrangThai(4);

                    } else if (trangThaiMoi == 8) {
                        hanhDongLog = "Hủy hóa đơn";
                        lyDoLog = "Hủy phiếu đặt bàn / Hủy hóa đơn";

                        // 🚨 ĐỒNG BỘ PHIẾU: Hủy hóa đơn -> Phiếu bị Hủy (2)
                        phieu.setTrangThai(2);
                    }
                    phieuDatBanRepository.save(phieu); // Lưu lại phiếu

                } else if (request.getTrangThaiPhieu() != null) {
                    if (request.getTrangThaiPhieu() == 3 && trangThaiCu < 4) {
                        trangThaiMoi = 4; // Trạng thái Phục vụ của Hóa Đơn
                        hoaDon.setTrangThaiHoaDon(trangThaiMoi);
                        hanhDongLog = "Khách nhận bàn";
                        lyDoLog = "Nhân viên xác nhận khách đã vào bàn";
                    }
                }

                // Ghi log
                if (!hanhDongLog.isEmpty()) {
                    ghiLichSu(hoaDon, request.getIdNhanVien(), hanhDongLog, lyDoLog, trangThaiCu, trangThaiMoi);
                }

                // Tiền mặt
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

    private String getTenMonFromEntity(ChiTietHoaDon ct) {
        if (ct.getIdChiTietMonAn() != null) return ct.getIdChiTietMonAn().getTenMon();
        if (ct.getIdSetLau() != null) return ct.getIdSetLau().getTenSetLau();
        return "Món không xác định";
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

        // Tìm nhân viên thực hiện (nếu có idNhanVien truyền lên từ FE)
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

        System.out.println(
                "Auto update: " + quaHan + " QUÁ HẠN, " + daHuy + " ĐÃ HỦY"
        );
    }

    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        phieuDatBanRepository.updateTrangThai(id, trangThai);
    }

    public List<BanTrangThaiResponse> getTrangThaiBanTheoNgay(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<PhieuDatBan> phieuList =
                phieuDatBanRepository.findPhieuTrongNgay(start, end);

        Map<Integer, Integer> banTrangThaiMap = new HashMap<>();

        for (PhieuDatBan phieu : phieuList) {
            Integer banId = phieu.getIdBanAn().getId();
            Integer trangThaiPhieu = phieu.getTrangThai();

            // Nếu đang sử dụng → BÀN CÓ KHÁCH
            if (trangThaiPhieu == 3) {
                banTrangThaiMap.put(banId, 1);
                continue;
            }

            // Nếu chưa có trạng thái và có đặt
            if (!banTrangThaiMap.containsKey(banId)) {
                if (trangThaiPhieu == 0 || trangThaiPhieu == 1) {
                    banTrangThaiMap.put(banId, 2);
                }
            }
        }

        return banTrangThaiMap.entrySet()
                .stream()
                .map(e -> new BanTrangThaiResponse(
                        e.getKey(),
                        e.getValue()
                ))
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

            // TẠM set để qua validate
            kv.setMaKhuVuc("TEMP");

            kv = khuVucRepository.save(kv);

            // Sau khi có ID mới generate mã chuẩn
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

    // Soft delete
    public void delete(Integer id) {
        KhuVuc kv = khuVucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        kv.setTrangThai(0);
        khuVucRepository.save(kv);
    }

    public List<BanAn> timDanhSachBanTrong(DatBanRequest request) {
        // Lấy danh sách bàn có sức chứa phù hợp
        List<BanAn> danhSachBanPhuHop = banAnRepository.findBanPhuHopChoDatBan(request.getSoNguoi());
        if (danhSachBanPhuHop.isEmpty()) return new java.util.ArrayList<>();

        // Tính toán khung giờ check TRÙNG HỆT như lúc Submit
        LocalDateTime thoiGianKhachDen = LocalDateTime.of(request.getNgayDat(), request.getGioDat());
        LocalDateTime start = thoiGianKhachDen.minusHours(2);
        LocalDateTime end = thoiGianKhachDen.plusHours(2);

        List<BanAn> danhSachBanTrong = new java.util.ArrayList<>();

        // Duyệt qua từng bàn, dùng đúng hàm check SQL để xem có ai đặt chưa
        for (BanAn ban : danhSachBanPhuHop) {
            // Dùng chung hàm với bước Submit -> Đảm bảo đồng nhất logic 100%
            boolean isTrung = phieuDatBanRepository.existsByTimeRange(ban, start, end);

            // Nếu KHÔNG bị trùng lịch thì mới ném vào danh sách cho FE hiển thị
            if (!isTrung) {
                danhSachBanTrong.add(ban);
            }
        }
        return danhSachBanTrong;
    }

    public List<BanAnResponse> checkBanTrong(DatBanRequest request) {
        List<BanAn> banTrongEntityList = timDanhSachBanTrong(request);
        return banTrongEntityList.stream()
                .map(BanAnResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackOn = Exception.class)
    public Map<String, Object> taoPhieuDatBanOnline(DatBanOnlineRequest request) {
        if (request.getIdBanAn() == null) {
            throw new RuntimeException("Vui lòng chọn bàn trước khi tiếp tục!");
        }

        BanAn banDuocChon = banAnRepository.findById(request.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại trong hệ thống!"));

        if (banDuocChon.getSoNguoiToiDa() < request.getSoNguoi()) {
            throw new RuntimeException("Bàn bạn chọn không đủ sức chứa cho " + request.getSoNguoi() + " người!");
        }

        // 1. Kiểm tra xem bàn này có bị trùng giờ đặt không (trước và sau 2 tiếng)
        LocalDateTime thoiGianKhachDen = request.getThoiGianDat();
        LocalDateTime start = thoiGianKhachDen.minusHours(2);
        LocalDateTime end = thoiGianKhachDen.plusHours(2);

        boolean isTrung = phieuDatBanRepository.existsByTimeRange(banDuocChon, start, end);
        if (isTrung) {
            throw new RuntimeException("Rất tiếc, bàn này đã có người đặt trong khung giờ bạn chọn. Vui lòng chọn bàn hoặc giờ khác!");
        }

        // 2. Xử lý khách hàng (Đã tối ưu để tương thích với Google Login)
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

        // 3. Tạo phiếu đặt bàn (Trạng thái 0: Pending)
        PhieuDatBan phieu = new PhieuDatBan();
        phieu.setIdBanAn(banDuocChon);
        phieu.setIdKhachHang(khachHang);
        phieu.setThoiGianDat(request.getThoiGianDat());
        phieu.setHinhThucDat(1);
        phieu.setSoLuongKhach(request.getSoNguoi());
        phieu.setTrangThai(0);
        phieu.setNguoiTao("Khách hàng");
        phieu.setMaDatBan("PDB" + System.currentTimeMillis());
        phieu.setNgayTao(java.time.LocalDateTime.now());
        phieu = phieuDatBanRepository.save(phieu);

        // * Đã xóa bỏ lệnh khóa bàn: banDuocChon.setTrangThai(2);

        // 4. Tạo Hóa Đơn Thanh Toán
        HoaDonThanhToan hoaDon = new HoaDonThanhToan();
        hoaDon.setIdKhachHang(khachHang);
        hoaDon.setIdBanAn(banDuocChon);
        hoaDon.setIdPhieuDatBan(phieu);

        String thongTinKhachNhap = String.format("[Hệ thống ghi nhận] Khách: %s | SĐT: %s | Email: %s. ",
                request.getFullName(), request.getPhone(), request.getEmail() != null ? request.getEmail() : "Không có");
        String ghiChuGoc = request.getGhiChu() != null ? "Ghi chú của khách: " + request.getGhiChu() : "";

        hoaDon.setGhiChu(thongTinKhachNhap + ghiChuGoc);
        hoaDon.setThoiGianTao(Instant.now());

        // Nhận tiền từ Request
        BigDecimal tongTien = request.getTongTien() != null ? request.getTongTien() : BigDecimal.ZERO;
        BigDecimal tienCoc = request.getTienCoc() != null ? request.getTienCoc() : BigDecimal.ZERO;

        hoaDon.setTongTienChuaGiam(tongTien);
        hoaDon.setTongTienThanhToan(tongTien);
        hoaDon.setTienCoc(tienCoc);
        hoaDon.setSoTienDaGiam(BigDecimal.ZERO);

        // XÁC ĐỊNH TRẠNG THÁI HÓA ĐƠN ĐỂ FRONTEND BIẾT CÓ CẦN GỌI VNPAY KHÔNG
        int trangThaiBanDau = (tienCoc.compareTo(BigDecimal.ZERO) > 0) ? 1 : 0;
        hoaDon.setTrangThaiHoaDon(trangThaiBanDau);

        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        List<LichSuHoaDon> dsLichSu = new ArrayList<>();

        LichSuHoaDon logTaoMoi = new LichSuHoaDon();
        logTaoMoi.setIdHoaDon(hoaDon);
        logTaoMoi.setHanhDong("Tạo hóa đơn online");
        logTaoMoi.setLyDoThucHien(String.format("Khách %s (%s/%s) tạo đơn đặt bàn qua Website", request.getFullName(), request.getPhone(), request.getEmail()));
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
                    } else {
                        DanhMucChiTiet temp = new DanhMucChiTiet();
                        temp.setId(mon.getIdChiTietMonAn());
                        ct.setIdChiTietMonAn(temp);
                    }
                }

                if (mon.getIdSetLau() != null) {
                    SetLau sl = setLauRepository.findById(mon.getIdSetLau()).orElse(null);
                    if (sl != null) {
                        tenMon = sl.getTenSetLau();
                        ct.setIdSetLau(sl);
                    } else {
                        SetLau temp = new SetLau();
                        temp.setId(mon.getIdSetLau());
                        ct.setIdSetLau(temp);
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

        // Lưu toàn bộ lịch sử trong 1 lần
        lichSuHoaDonRepository.saveAll(dsLichSu);

        // 7. Lấy lại hóa đơn để nhận giá trị Tổng Tiền mới nhất (do Trigger cập nhật)
        HoaDonThanhToan hoaDonMoiNhat = hoaDonThanhToanRepository.findById(hoaDon.getId()).orElse(hoaDon);

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

        // ===============================
        // 1️⃣ Nếu chọn khách cũ
        // ===============================
        if (req.getIdKhachHang() != null) {

            khachHang = khachHangRepository.findById(req.getIdKhachHang())
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy khách hàng"));

        } else {

            // ===============================
            // 2️⃣ Nếu là khách mới
            // ===============================

            if (req.getSoDienThoai() == null || req.getSoDienThoai().isBlank()) {
                throw new RuntimeException("Số điện thoại không được để trống");
            }

            Optional<KhachHang> existing =
                    khachHangRepository.findBySoDienThoai(req.getSoDienThoai());

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
                khachHangRepository.save(khachHang);
            }
        }

        // ===============================
        // 3️⃣ Kiểm tra bàn
        // ===============================

        BanAn ban = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bàn"));

        // ===============================
        // 4️⃣ Check trùng giờ
        // ===============================

        LocalDateTime start = req.getThoiGianDat().minusHours(2);
        LocalDateTime end   = req.getThoiGianDat().plusHours(2);

        boolean isTrung = phieuDatBanRepository.existsByTimeRange(ban, start, end);

        if (isTrung) {
            throw new RuntimeException("Bàn đã có lịch trong vòng 2 tiếng");
        }

        // ===============================
        // 5️⃣ Tạo phiếu
        // ===============================

        PhieuDatBan pdb = new PhieuDatBan();
        pdb.setIdBanAn(ban);
        pdb.setIdKhachHang(khachHang);
        pdb.setThoiGianDat(req.getThoiGianDat());
        pdb.setHinhThucDat(req.getHinhThucDat());
        pdb.setSoLuongKhach(req.getSoLuongKhach());
        pdb.setTrangThai(0);

        PhieuDatBan saved = phieuDatBanRepository.save(pdb);

        ban.setTrangThai(1);
        banAnRepository.save(ban);

        // ===============================
        // 6️⃣ Gửi mail + auto confirm
        // ===============================

        boolean daGuiMail = false;

        if (khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {

            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .soDienThoai(khachHang.getSoDienThoai())
                    .email(khachHang.getEmail())
                    .thoiGianDat(req.getThoiGianDat()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .tenBan(ban.getMaBan())
                    .khuVuc(ban.getIdKhuVuc() != null
                            ? ban.getIdKhuVuc().getTenKhuVuc() + " - Tầng " + ban.getIdKhuVuc().getTang()
                            : "N/A")
                    .soLuongKhach(req.getSoLuongKhach())
                    .maPhieuDatBan("PDB-" + saved.getId())
                    .build();

            try {
                emailDatBanService.sendXacNhanDatBanSync(emailDto); // ← gọi bản sync

                // ✅ Gửi mail OK → tự động xác nhận phiếu
                saved.setTrangThai(1); // 1 = Đã xác nhận
                phieuDatBanRepository.save(saved);

                daGuiMail = true;
                log.info("✅ Tự động xác nhận phiếu PDB-{} sau khi gửi mail", saved.getId());

            } catch (Exception e) {
                // ❌ Gửi mail lỗi → giữ trạng thái 0, không crash API
                log.warn("⚠️ Gửi mail thất bại, phiếu PDB-{} giữ trạng thái chờ xác nhận: {}",
                        saved.getId(), e.getMessage());
            }

        } else {
            // Không có email → vẫn tạo phiếu nhưng trạng thái 0 chờ xác nhận thủ công
            log.info("ℹ️ Khách không có email, phiếu PDB-{} chờ xác nhận thủ công", saved.getId());
        }

        // ===============================
        // 7️⃣ Trả về response cho FE
        // ===============================

        return CreatePhieuDatBanFullResponse.builder()
                .idPhieuDatBan(saved.getId())
                .maPhieuDatBan("PDB-" + saved.getId())
                .daGuiMail(daGuiMail)
                .emailGuiToi(daGuiMail ? khachHang.getEmail() : null)
                .build();
    }

    // ========== Lấy tất cả khách hàng cho multiselect ==========
    public List<KhachHangSelectDTO> getAllForSelect() {
        return khachHangRepository.findAll()
                .stream()
                .map(this::toSelectDTO)
                .collect(Collectors.toList());
    }

    // ========== Tìm kiếm theo keyword (tên hoặc SĐT) ==========
    public List<KhachHangSelectDTO> searchByKeyword(String keyword) {
        return khachHangRepository.searchByKeyword(keyword)
                .stream()
                .map(this::toSelectDTO)
                .collect(Collectors.toList());
    }

    // ========== Tìm theo SĐT chính xác ==========
    public KhachHangSelectDTO findBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai)
                .map(this::toSelectDTO)
                .orElse(null);
    }

    // ========== Mapper Entity -> DTO ==========
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
        // 1. Tìm hóa đơn
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // 2. Tìm phiếu đặt bàn liên kết
        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
        if (phieu == null) {
            throw new RuntimeException("Hóa đơn này không có phiếu đặt bàn liên kết!");
        }

        // Kiểm tra xem trạng thái có hợp lệ không (Chỉ xử lý đơn mới tạo hoặc chờ cọc/đã cọc)
        if (hoaDon.getTrangThaiHoaDon() != 0 && hoaDon.getTrangThaiHoaDon() != 2) {
            throw new RuntimeException("Trạng thái hóa đơn không hợp lệ để xác nhận!");
        }

        Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

        // 3. Cập nhật trạng thái
        hoaDon.setTrangThaiHoaDon(3); // 3: Hóa đơn đã xác nhận
        phieu.setTrangThai(1);        // 1: Phiếu đã xác nhận

        // 4. Lưu vào CSDL
        hoaDonThanhToanRepository.save(hoaDon);
        phieuDatBanRepository.save(phieu);

        // 5. Ghi lịch sử (Dùng lại hàm ghiLichSu có sẵn của bạn)
        ghiLichSu(hoaDon, idNhanVien,
                "Xác nhận & Gửi Mail",
                "Nhân viên xác nhận đơn đặt bàn và gửi email cho khách",
                trangThaiCu, 3);

        // 6. Xử lý Gửi Email
        KhachHang khachHang = hoaDon.getIdKhachHang();
        BanAn banAn = hoaDon.getIdBanAn();

        if (khachHang != null && khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {
            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .soDienThoai(khachHang.getSoDienThoai())
                    .email(khachHang.getEmail())
                    .thoiGianDat(phieu.getThoiGianDat() != null ?
                            phieu.getThoiGianDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Chưa xác định")
                    .tenBan(banAn != null ? banAn.getMaBan() : "Chưa xếp bàn")
                    .khuVuc((banAn != null && banAn.getIdKhuVuc() != null) ?
                            banAn.getIdKhuVuc().getTenKhuVuc() + " - Tầng " + banAn.getIdKhuVuc().getTang() : "Chưa xác định")
                    .soLuongKhach(phieu.getSoLuongKhach())
                    .maPhieuDatBan(phieu.getMaDatBan() != null ? phieu.getMaDatBan() : "PDB-" + phieu.getId())
                    .build();

            try {
                emailDatBanService.sendXacNhanDatBanSync(emailDto);
                log.info("✅ Đã gửi mail xác nhận thành công cho hóa đơn: {}", idHoaDon);
            } catch (Exception e) {
                log.error("⚠️ Lỗi gửi mail cho hóa đơn {}: {}", idHoaDon, e.getMessage());
                // Vẫn cho phép cập nhật trạng thái thành công, nhưng ném lỗi để FE biết mail có vấn đề
                throw new RuntimeException("Xác nhận thành công nhưng lỗi khi gửi mail: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Khách hàng chưa cung cấp địa chỉ Email để gửi!");
        }
    }
}
