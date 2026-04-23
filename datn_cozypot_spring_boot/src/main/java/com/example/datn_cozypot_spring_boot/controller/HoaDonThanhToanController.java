package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.GopBanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.ThemBanPhuRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.XepBanRequest;
import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.ChiTietHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.HoaDonThanhToanService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuThanhToanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hoa-don-thanh-toan")
@CrossOrigin(origins = "http://localhost:5173")
public class HoaDonThanhToanController {
    private static final Logger log = LoggerFactory.getLogger(HoaDonThanhToanController.class);
    private final HoaDonThanhToanService hoaDonThanhToanService;

    private final ChiTietHoaDonService chiTietHoaDonService;

    private final LichSuHoaDonService lichSuHoaDonService;
    private final phieuDatBanBanAnRepository phieuDatBanBanAnRepository;


    private final LichSuThanhToanService lichSuThanhToanService;
    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final PhieuDatBanRepository phieuDatBanRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final DatBanService datBanService;
    private final NhanVienRepository nhanVienRepository;
    private final BanAnRepository banAnRepository;
    private final ThamSoHeThongRepository thamSoHeThongRepository;

    @GetMapping("/get-all")
    public Page<HoaDonThanhToanResponse> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("thoiGianTao").ascending());
        return hoaDonThanhToanService.getAllHoaDon(pageable);
    }

    @GetMapping("/search")
    public Page<HoaDonThanhToanResponse> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String tuNgayTao,
            @RequestParam(required = false) String denNgayTao,
            @RequestParam(required = false) String tuNgayDat,
            @RequestParam(required = false) String denNgayDat,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {

        Instant startTao = (tuNgayTao != null && !tuNgayTao.trim().isEmpty()) ? Instant.parse(tuNgayTao) : null;
        Instant endTao = (denNgayTao != null && !denNgayTao.trim().isEmpty()) ? Instant.parse(denNgayTao) : null;

        LocalDateTime startDat = (tuNgayDat != null && !tuNgayDat.trim().isEmpty()) ? LocalDateTime.parse(tuNgayDat) : null;
        LocalDateTime endDat = (denNgayDat != null && !denNgayDat.trim().isEmpty()) ? LocalDateTime.parse(denNgayDat) : null;

        if (key != null) {
            key = key.trim();
        }

        Pageable pageable = PageRequest.of(page, size);

        return hoaDonThanhToanService.searchHoaDon(key, trangThai, startTao, endTao, startDat, endDat, pageable
        );
    }

    @PutMapping("/xac-nhan-hoan-tien/{idHoaDon}")
    public ResponseEntity<?> xacNhanHoanTien(
            @PathVariable Integer idHoaDon,
            @RequestParam(required = false) Integer idNhanVien) {
        try {
            hoaDonThanhToanService.xacNhanHoanTien(idHoaDon, idNhanVien);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Đã xác nhận hoàn tiền thành công cho hóa đơn #" + idHoaDon
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active-by-phieu/{idPhieu}")
    public ResponseEntity<?> getActiveBillByPhieu(@PathVariable Integer idPhieu) {
        // 1. Tìm hóa đơn theo ID phiếu
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(idPhieu);

        if (hoaDon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Phiếu này chưa gọi món/tạo hóa đơn"));
        }

        // 2. Khởi tạo DTO Response
        PhieuDatBanResponse response = new PhieuDatBanResponse();

        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
        KhachHang khach = hoaDon.getIdKhachHang();

        // ==========================================
        // MAP THÔNG TIN PHIẾU & KHÁCH HÀNG
        // ==========================================
        if (phieu != null) {
            response.setId(phieu.getId());
            response.setMaPhieu(phieu.getMaDatBan()); // Thay getMaDatBan() bằng getter đúng của bạn
            response.setThoiGianDat(phieu.getThoiGianDat());
            response.setThoiGianNhanBan(phieu.getThoiGianNhanBan());
            response.setSoNguoi(phieu.getSoLuongKhach());
            response.setTrangThai(phieu.getTrangThai());

            // Map Danh sách bàn
            List<PhieuDatBanResponse.BanAnInfo> danhSachBanInfo = new ArrayList<>();
            if (phieu.getDsBanAn() != null) {
                for (PhieuDatBanBanAn link : phieu.getDsBanAn()) {
                    BanAn ban = link.getBanAn();
                    PhieuDatBanResponse.BanAnInfo banInfo = new PhieuDatBanResponse.BanAnInfo(
                            ban.getId(),
                            ban.getMaBan(),
                            ban.getIdKhuVuc().getTenKhuVuc(),
                            ban.getIdKhuVuc().getTang()
                    );
                    danhSachBanInfo.add(banInfo);
                }
            }
            response.setDanhSachBan(danhSachBanInfo);

            // Lấy bàn đầu tiên làm bàn đại diện
            if (!danhSachBanInfo.isEmpty()) {
                response.setIdBanAn(danhSachBanInfo.get(0).getId());
                response.setMaBan(danhSachBanInfo.get(0).getMaBan());
                response.setTenKhuVuc(danhSachBanInfo.get(0).getTenKhuVuc());
                response.setTang(danhSachBanInfo.get(0).getTang());
            }
        }

        if (khach != null) {
            response.setIdKhachHang(khach.getId());
            response.setTenKhachHang(khach.getTenKhachHang());
            response.setSdtKhachHang(khach.getSoDienThoai());
        }

        // ==========================================
        // MAP THÔNG TIN HÓA ĐƠN
        // ==========================================
        List<PhieuDatBanResponse.ChiTietMonResponse> chiTietList = new ArrayList<>();

        if (hoaDon.getChiTietHoaDons() != null) {
            for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
                PhieuDatBanResponse.ChiTietMonResponse monRes = new PhieuDatBanResponse.ChiTietMonResponse();

                monRes.setIdChiTietHd(ct.getId());
                monRes.setSoLuong(ct.getSoLuong());
                monRes.setDonGia(ct.getDonGiaTaiThoiDiemBan());
                monRes.setThanhTien(ct.getThanhTien());
                monRes.setTrangThaiMon(ct.getTrangThaiMon());
                monRes.setGhiChu(ct.getGhiChuMon());

                // Phân loại Món lẻ hay Set lẩu an toàn
                if (ct.getIdChiTietMonAn() != null) {
                    monRes.setType("FOOD");
                    monRes.setId(ct.getIdChiTietMonAn().getId());
                    monRes.setIdChiTietMonAn(ct.getIdChiTietMonAn().getId());
                    monRes.setMaMon(ct.getIdChiTietMonAn().getMaMon());
                    monRes.setTenMon(ct.getIdChiTietMonAn().getTenMon());
                    monRes.setApDungLoaiVat(ct.getIdChiTietMonAn().getDanhMuc().getLoaiVatApDung());
                } else if (ct.getIdSetLau() != null) {
                    monRes.setType("SET");
                    monRes.setId(ct.getIdSetLau().getId());
                    monRes.setIdSetLau(ct.getIdSetLau().getId());
                    monRes.setMaSetLau(ct.getIdSetLau().getMaSetLau());
                    monRes.setTenMon(ct.getIdSetLau().getTenSetLau());
                    monRes.setApDungLoaiVat(8);
                }

                chiTietList.add(monRes);
            }
        }
        response.setChiTiet(chiTietList);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<HoaDonThanhToanResponse> getById(@PathVariable Integer id) {
        HoaDonThanhToanResponse response = hoaDonThanhToanService.getHoaDonById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/xep-ban")
    @Transactional // 🚨 BẮT BUỘC PHẢI CÓ ANNOTATION NÀY ĐỂ JPA TỰ LƯU THAY ĐỔI
    public ResponseEntity<?> xepBanChoPhieuCu(@RequestBody XepBanRequest request) {
        PhieuDatBan phieu = phieuDatBanRepository.findById(request.getIdPhieuDatBan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

        // Khởi tạo mảng nếu phiếu chưa từng có bàn nào
        if (phieu.getDsBanAn() == null) {
            phieu.setDsBanAn(new HashSet<>());
        }

        List<Integer> listIdBanMoi = request.getDanhSachIdBanAn();

        // =======================================================
        // BƯỚC 1: XÓA CÁC BÀN BỊ KHÁCH GỠ BỎ TRONG LÚC "CHỌN LẠI"
        // =======================================================
        Iterator<PhieuDatBanBanAn> iterator = phieu.getDsBanAn().iterator();
        while (iterator.hasNext()) {
            PhieuDatBanBanAn linkCu = iterator.next();
            BanAn banCu = linkCu.getBanAn();

            // Nếu bàn cũ này không còn nằm trong danh sách bàn mới khách chọn
            if (!listIdBanMoi.contains(banCu.getId())) {
                // Trả lại trạng thái bàn về 0 (Trống)
                banCu.setTrangThai(0);
                banAnRepository.save(banCu);

                // Xóa mối quan hệ khỏi phiếu hiện tại
                iterator.remove();
            }
        }

        // =======================================================
        // BƯỚC 2: CHIA SỐ LƯỢNG KHÁCH CHO CÁC BÀN MỚI CHỌN
        // =======================================================
        int soBan = listIdBanMoi.size();
        int khachMoiBan = phieu.getSoLuongKhach() / soBan;
        int khachDu = phieu.getSoLuongKhach() % soBan;

        for (int i = 0; i < soBan; i++) {
            Integer idBanHienTai = listIdBanMoi.get(i);
            int soKhachNgoi = khachMoiBan + (i == 0 ? khachDu : 0);

            BanAn ban = banAnRepository.findById(idBanHienTai)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn: " + idBanHienTai));

            // Đổi trạng thái bàn thành Đã Đặt (2)
            ban.setTrangThai(2);
            banAnRepository.save(ban);

            // =======================================================
            // 🚨 BƯỚC 3 (SỬA LỖI): TÌM XEM BÀN NÀY ĐÃ CÓ TRONG PHIẾU CHƯA
            // =======================================================
            PhieuDatBanBanAn linkTonTai = phieu.getDsBanAn().stream()
                    .filter(l -> l.getBanAn().getId().equals(idBanHienTai))
                    .findFirst()
                    .orElse(null);

            if (linkTonTai != null) {
                // TRƯỜNG HỢP 1: Bàn đã có sẵn (Khách giữ lại bàn cũ)
                // -> Chỉ cập nhật số người, TUYỆT ĐỐI KHÔNG DÙNG "new"
                linkTonTai.setSoNguoiNgoi(soKhachNgoi);
            } else {
                // TRƯỜNG HỢP 2: Bàn mới tinh
                // -> Lúc này mới được phép tạo "new" object
                PhieuDatBanBanAn linkMoi = new PhieuDatBanBanAn();
                linkMoi.setBanAn(ban);
                linkMoi.setPhieuDatBan(phieu);
                linkMoi.setSoNguoiNgoi(soKhachNgoi);

                PhieuDatBanBanAnId linkId = new PhieuDatBanBanAnId();
                linkId.setIdBanAn(ban.getId());
                linkId.setIdPhieuDatBan(phieu.getId());
                linkMoi.setId(linkId);

                phieu.getDsBanAn().add(linkMoi);
            }
        }

        // Lưu lại phiếu với những thay đổi mới nhất
        phieuDatBanRepository.save(phieu);

        return ResponseEntity.ok(Map.of("message", "Xếp bàn thành công"));
    }

    @GetMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<?> getHistory(@PathVariable Integer idHoaDon) {
        List<LichSuHoaDon> list = lichSuHoaDonRepository.findByIdHoaDon_Id(idHoaDon);

        // Map sang DTO để có tên nhân viên và format thời gian chuẩn
        List<Map<String, Object>> response = list.stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("hanhDong", log.getHanhDong());
            map.put("thoiGianThucHien", log.getThoiGianThucHien()); // Instant hoặc LocalDateTime
            map.put("lyDoThucHien", log.getLyDoThucHien());

            // Lấy tên nhân viên từ object NhanVien
            if (log.getIdNhanVien() != null) {
                map.put("tenNhanVien", log.getIdNhanVien().getHoTenNhanVien());
            } else {
                map.put("tenNhanVien", "Hệ thống");
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/xac-nhan-thanh-toan")
    public ResponseEntity<?> xacNhanThanhToan(@RequestBody LichSuHoaDonRequest request) {
        try {
            hoaDonThanhToanService.thanhToanHoaDon(request);
            return ResponseEntity.ok("Thanh toán thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/hoan-tat")
    public ResponseEntity<?> hoanTatHoaDon(@RequestBody LichSuHoaDonRequest request) {
        try {
            hoaDonThanhToanService.hoanTatHoaDon(request);
            return ResponseEntity.ok("Hoàn tất hóa đơn và giải phóng bàn thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/xac-nhan-va-gui-mail/{idHoaDon}")
    public ResponseEntity<?> xacNhanVaGuiMail(@PathVariable Integer idHoaDon) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            NhanVien nv = nhanVienRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên thao tác!"));

            Integer idNhanVien = nv.getId();

            datBanService.xacNhanVaGuiMail(idHoaDon, idNhanVien);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Đã xác nhận hóa đơn và gửi email thành công!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/lich-su/{idHoaDon}")
    public ResponseEntity<List<LichSuHoaDonResponse>> getLichSuDonHang(@PathVariable Integer idHoaDon) {
        List<LichSuHoaDonResponse> history = lichSuHoaDonService.layLichSuDonHang(idHoaDon);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/lich-su-thanh-toan/{id}")
    public ResponseEntity<List<LichSuThanhToanResponse>> getLichSuThanhToan(@PathVariable Integer id){
        return ResponseEntity.ok(lichSuThanhToanService.getAllLichSuThanhToan(id));
    }

    @PutMapping("/huy-don")
    public ResponseEntity<?> huyDon(@RequestBody LichSuHoaDonRequest request) {
        try {
            hoaDonThanhToanService.huyHoaDon(request);
            return ResponseEntity.ok("Hủy đơn thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/chi-tiet-hoa-don/{idHoaDon}")
    public List<ChiTietHoaDonResponse> getAllChiTiet(@PathVariable Integer idHoaDon){
        return chiTietHoaDonService.getAllChiTietHoaDon(idHoaDon);
    }

    @GetMapping("/chi-tiet-hoa-don/chi-tiet-set-lau/{idSetLau}")
    public ResponseEntity<List<ChiTietSetLauResponse>> getChiTietSetLau(@PathVariable Integer idSetLau){
        return ResponseEntity.ok(chiTietHoaDonService.getChiTietSetLau(idSetLau));
    }

    @PutMapping("/chi-tiet-hoa-don/cap-nhat-da-len/{id}")
    public ResponseEntity<?> updateMonDaLen(@PathVariable Integer id) {
        try {
            chiTietHoaDonService.updateToServed(id);
            return ResponseEntity.ok("Đã cập nhật món");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/chi-tiet-hoa-don/cap-nhat-tat-ca-da-len/{idHoaDon}")
    public ResponseEntity<?> updateTatCaDaLen(@PathVariable Integer idHoaDon) {
        try {
            chiTietHoaDonService.updateAllToServed(idHoaDon);
            return ResponseEntity.ok("Đã cập nhật tất cả món");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tao-don")
    public ResponseEntity<?> createOrder(@RequestBody HoaDonThanhToanRequest request) {
        try {
            hoaDonThanhToanService.createOrder(request);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Lên đơn món ăn thành công!");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/active-by-ban/{idBanAn}")
    public ResponseEntity<PhieuDatBanResponse> findActivePhieuByBanAn(
            @PathVariable Integer idBanAn,
            @RequestParam(required = false) Integer idPhieu) {

        // 1. Tìm Bàn ăn mà nhân viên vừa click vào
        BanAn banAn = banAnRepository.findById(idBanAn).orElse(null);
        if (banAn == null) return ResponseEntity.notFound().build();

        // 2. Tìm Phiếu Đặt Bàn chứa bàn này
        List<PhieuDatBan> danhSachPhieu = phieuDatBanRepository.findByDsBanAn_BanAn_IdAndTrangThaiInOrderByThoiGianDatAsc(idBanAn, Arrays.asList(0, 1, 3));

        if (danhSachPhieu == null || danhSachPhieu.isEmpty()) {
            return ResponseEntity.noContent().build(); // Bàn trống
        }
        Integer soNguoiBanNay = 0;
        PhieuDatBan phieu = (idPhieu != null)
                ? danhSachPhieu.stream().filter(p -> p.getId().equals(idPhieu)).findFirst().orElse(danhSachPhieu.get(0))
                : danhSachPhieu.get(0);

        // 3. Map các thông tin cơ bản
        PhieuDatBanResponse res = new PhieuDatBanResponse();
        res.setId(phieu.getId());
        res.setMaPhieu(phieu.getMaDatBan());
        res.setThoiGianDat(phieu.getThoiGianDat());
        Integer tongNguoiTaiBan = phieuDatBanBanAnRepository.getTongSoNguoiDangNgoiTaiBan(idBanAn);
        res.setSoNguoi(tongNguoiTaiBan);
        res.setTrangThai(phieu.getTrangThai());
        res.setThoiGianNhanBan(phieu.getThoiGianNhanBan());


        if (phieu.getIdKhachHang() != null) {
            res.setIdKhachHang(phieu.getIdKhachHang().getId());
            res.setTenKhachHang(phieu.getIdKhachHang().getTenKhachHang());
            res.setSdtKhachHang(phieu.getIdKhachHang().getSoDienThoai());
        } else {
            res.setTenKhachHang("Khách vãng lai");
        }

        // Set Bàn hiện tại (Bàn được click)
        res.setIdBanAn(banAn.getId());
        res.setMaBan(banAn.getMaBan());
        if (banAn.getIdKhuVuc() != null) {
            res.setTenKhuVuc(banAn.getIdKhuVuc().getTenKhuVuc());
            res.setTang(banAn.getIdKhuVuc().getTang());
        }

        // ===============================================================
        // 🚨 4. FIX LỖI Ở ĐÂY: XỬ LÝ N-N, DANH SÁCH BÀN VÀ BÀN CHÍNH/PHỤ
        // ===============================================================
        Set<PhieuDatBanBanAn> dsLienKetBanAn = phieu.getDsBanAn(); // 🚨 Lấy bảng trung gian

        // Tạo List các bàn để gửi lên VueJS
        List<PhieuDatBanResponse.BanAnInfo> listBanInfo = new ArrayList<>();

        BanAn banChinh = null;

        if (dsLienKetBanAn != null && !dsLienKetBanAn.isEmpty()) {

            // A. Tìm Bàn chính (Quy ước: bàn có ID nhỏ nhất là bàn gốc)
            PhieuDatBanBanAn lienKetBanChinh = dsLienKetBanAn.stream()
                    .min(Comparator.comparing(link -> link.getBanAn().getId()))
                    .orElse(null);

            if (lienKetBanChinh != null) {
                banChinh = lienKetBanChinh.getBanAn();
            }

            // B. Map toàn bộ bàn vào listBanInfo & ĐỒNG THỜI tìm số người của Bàn đang Click
            for (PhieuDatBanBanAn link : dsLienKetBanAn) {
                BanAn b = link.getBanAn();

                // 🚨 TÌM THẤY BÀN ĐANG CLICK -> LẤY SỐ NGƯỜI
                if (b.getId().equals(idBanAn)) {
                    // Nếu lấy được từ bảng trung gian thì dùng, không thì lấy mặc định 0
                    soNguoiBanNay = link.getSoNguoiNgoi() != null ? link.getSoNguoiNgoi() : 0;
                }

                PhieuDatBanResponse.BanAnInfo info = new PhieuDatBanResponse.BanAnInfo();
                info.setId(b.getId());
                info.setMaBan(b.getMaBan());
                if (b.getIdKhuVuc() != null) {
                    info.setTenKhuVuc(b.getIdKhuVuc().getTenKhuVuc());
                    info.setTang(b.getIdKhuVuc().getTang());
                }
                listBanInfo.add(info);
            }
        }

        res.setDanhSachBan(listBanInfo);

        // 🚨 GÁN SỐ NGƯỜI RIÊNG CỦA BÀN VÀO DTO TRẢ VỀ CHO VUEJS
        res.setSoNguoiBanNay(soNguoiBanNay);

        // Xét xem bàn đang click có phải là bàn chính không
        if (banChinh != null) {
            if (!banAn.getId().equals(banChinh.getId())) {
                // Click vào Bàn Phụ
                res.setIsBanPhu(true);
                res.setTenBanChinh(banChinh.getMaBan());
                res.setIdBanChinh(banChinh.getId());
            } else {
                // Click vào Bàn Chính
                res.setIsBanPhu(false);
                res.setTenBanChinh(banChinh.getMaBan());
                res.setIdBanChinh(banChinh.getId());
            }
        }

        // ===============================================================
        // 5. MAP HÓA ĐƠN & MÓN ĂN
        // ===============================================================
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieu.getId());

        if (hoaDon != null && hoaDon.getTrangThaiHoaDon() < 6) {
            res.setIdHoaDon(hoaDon.getId());
            res.setTongTienChuaGiam(hoaDon.getTongTienChuaGiam());
            res.setSoTienDaGiam(hoaDon.getSoTienDaGiam());
            res.setTienCoc(hoaDon.getTienCoc());
            res.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
            if (hoaDon.getIdPhieuGiamGia() != null) {
                res.setIdPhieuGiamGia(hoaDon.getIdPhieuGiamGia().getId());
                res.setMaPhieuGiamGia(hoaDon.getIdPhieuGiamGia().getCodeGiamGia()); // Lưu ý: Tên trường trong Entity PhieuGiamGia thường là codeGiamGia
            }

            List<ChiTietHoaDon> chiTietHD = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
            if (chiTietHD != null) {
                List<PhieuDatBanResponse.ChiTietMonResponse> dsMon = chiTietHD.stream()
                        .filter(m -> m.getTrangThaiMon() != null && m.getTrangThaiMon() != 0)
                        .map(item -> {
                            PhieuDatBanResponse.ChiTietMonResponse dto = new PhieuDatBanResponse.ChiTietMonResponse();
                            dto.setIdChiTietHd(item.getId());
                            dto.setSoLuong(item.getSoLuong());
                            dto.setDonGia(item.getDonGiaTaiThoiDiemBan());
                            dto.setThanhTien(item.getThanhTien());
                            dto.setTrangThaiMon(item.getTrangThaiMon());

                            // Lấy ghi chú chuẩn
                            String ghiChuRaw = item.getGhiChuMon() != null ? item.getGhiChuMon() : "";
                            dto.setGhiChu(ghiChuRaw);

                            if (item.getIdChiTietMonAn() != null) {
                                dto.setTenMon(item.getIdChiTietMonAn().getTenMon());
                                dto.setId(item.getIdChiTietMonAn().getId());
                                dto.setType("FOOD");
                                dto.setMaMon(item.getIdChiTietMonAn().getMaMon());

                                Integer vatType = 1;
                                try {
                                    if (item.getIdChiTietMonAn().getDanhMuc() != null &&
                                            item.getIdChiTietMonAn().getDanhMuc().getLoaiVatApDung() != null) {

                                        vatType = item.getIdChiTietMonAn().getDanhMuc().getLoaiVatApDung();
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                                dto.setApDungLoaiVat(vatType);

                            } else if (item.getIdSetLau() != null) {
                                dto.setTenMon(item.getIdSetLau().getTenSetLau());
                                dto.setId(item.getIdSetLau().getId());
                                dto.setType("SET");
                                dto.setApDungLoaiVat(1);
                                dto.setMaSetLau(item.getIdSetLau().getMaSetLau());
                            }
                            return dto;
                        }).collect(Collectors.toList());
                res.setChiTiet(dsMon);
            } else {
                res.setChiTiet(new ArrayList<>());
            }
        } else {
            res.setTienCoc(hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO);
            res.setChiTiet(new ArrayList<>());
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/danh-sach-phieu-tai-ban/{idBanAn}")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachPhieuTaiBan(@PathVariable Integer idBanAn) {
        List<PhieuDatBanBanAn> links = phieuDatBanBanAnRepository.findActiveLinksByBanId(idBanAn);
        List<Map<String, Object>> result = new ArrayList<>();

        for(PhieuDatBanBanAn link : links) {
            Map<String, Object> map = new HashMap<>();
            map.put("idPhieu", link.getPhieuDatBan().getId());

            // Trích xuất tên khách hàng (nếu có)
            String tenKhach = link.getPhieuDatBan().getIdKhachHang() != null
                    ? link.getPhieuDatBan().getIdKhachHang().getTenKhachHang()
                    : "Khách vãng lai";

            map.put("tenKhachHang", tenKhach);
            map.put("soNguoi", link.getSoNguoiNgoi());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/huy-phieu-cho")
    @Transactional
    public ResponseEntity<?> huyPhieuChoChuaCheckIn(@RequestBody Map<String, Object> payload) {
        try {
            Integer idPhieu = Integer.parseInt(payload.get("idPhieu").toString());
            Boolean isRefundable = Boolean.parseBoolean(payload.get("isRefundable").toString());

            // 1. Tìm phiếu đặt bàn
            PhieuDatBan phieu = phieuDatBanRepository.findById(idPhieu)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            // 2. Chuyển trạng thái phiếu về 0 (Đã hủy)
            phieu.setTrangThai(2);
            phieuDatBanRepository.save(phieu);

            // 3. Tìm hóa đơn ăn theo phiếu này
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(idPhieu);

            if (hoaDon != null) {
                if (isRefundable) {
                    // HOÀN TIỀN (Trạng thái 9)
                    hoaDon.setTrangThaiHoaDon(9);
                    // Đẩy toàn bộ tiền cọc sang cột tiền hoàn trả
                    BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;
                    hoaDon.setTienHoanTra(tienCoc);
                } else {
                    // KHÔNG HOÀN TIỀN (Trạng thái 8)
                    hoaDon.setTrangThaiHoaDon(8);
                    hoaDon.setTienHoanTra(BigDecimal.ZERO);
                }
                hoaDonThanhToanRepository.save(hoaDon);
            }

            return ResponseEntity.ok(Map.of("message", "Hủy phiếu thành công"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi Backend: " + e.getMessage()));
        }
    }

    // Thêm vào Controller tương ứng
    @PostMapping("/gop-ban")
    public ResponseEntity<?> gopBanThanhToan(@RequestBody GopBanRequest request) {
        try {
            hoaDonThanhToanService.gopBan(request);
            return ResponseEntity.ok(Map.of("message", "Gộp bàn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{idHoaDonGoc}/them-ban-phu")
    public ResponseEntity<?> themBanPhu(@PathVariable Integer idHoaDonGoc, @RequestBody ThemBanPhuRequest payload) {
        try {
            // 🚨 In ra console để kiểm tra xem Backend có nhận được số người không
            System.out.println("=== API THÊM BÀN PHỤ ===");
            System.out.println("ID Hóa đơn gốc: " + idHoaDonGoc);
            System.out.println("ID Bàn mới: " + payload.getIdBanMoi());
            System.out.println("Số người FE gửi lên: " + payload.getSoNguoi());
            System.out.println("========================");

            // Truyền thẳng số người xuống Service
            hoaDonThanhToanService.themBanVaoHoaDon(idHoaDonGoc, payload.getIdBanMoi(), payload.getSoNguoi());

            return ResponseEntity.ok(Map.of("message", "Đã thêm bàn phụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
