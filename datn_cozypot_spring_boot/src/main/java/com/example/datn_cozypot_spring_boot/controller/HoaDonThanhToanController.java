package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.GopBanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
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

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<HoaDonThanhToanResponse> getById(@PathVariable Integer id) {
        HoaDonThanhToanResponse response = hoaDonThanhToanService.getHoaDonById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
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

        PhieuDatBan phieu = (idPhieu != null)
                ? danhSachPhieu.stream().filter(p -> p.getId().equals(idPhieu)).findFirst().orElse(danhSachPhieu.get(0))
                : danhSachPhieu.get(0);

        // 3. Map các thông tin cơ bản
        PhieuDatBanResponse res = new PhieuDatBanResponse();
        res.setId(phieu.getId());
        res.setMaPhieu(phieu.getMaDatBan());
        res.setThoiGianDat(phieu.getThoiGianDat());
        res.setSoNguoi(phieu.getSoLuongKhach());
        res.setTrangThai(phieu.getTrangThai());

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
        Set<BanAn> cacBanTrongPhieu = phieu.getBanAns();

        // Tạo List các bàn để gửi lên VueJS
        List<PhieuDatBanResponse.BanAnInfo> listBanInfo = new ArrayList<>();

        // Quy ước: Bàn đầu tiên được thêm vào Set (hoặc có ID nhỏ nhất) sẽ là Bàn Chính
        BanAn banChinh = null;

        if (cacBanTrongPhieu != null && !cacBanTrongPhieu.isEmpty()) {
            // Tìm Bàn chính (Ví dụ lấy bàn có ID nhỏ nhất để luôn cố định 1 bàn làm gốc)
            banChinh = cacBanTrongPhieu.stream()
                    .min(Comparator.comparing(BanAn::getId))
                    .orElse(null);

            // Map toàn bộ bàn vào listBanInfo
            for (BanAn b : cacBanTrongPhieu) {
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
            res.setVatApDung(hoaDon.getVatApDung() != null ? Double.valueOf(hoaDon.getVatApDung()) : 10.0);

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
    public ResponseEntity<?> themBanPhu(@PathVariable Integer idHoaDonGoc, @RequestBody Map<String, Integer> payload) {
        try {
            hoaDonThanhToanService.themBanVaoHoaDon(idHoaDonGoc, payload.get("idBanMoi"));
            return ResponseEntity.ok(Map.of("message", "Đã thêm bàn phụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
