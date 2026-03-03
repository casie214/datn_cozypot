package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hoa-don-thanh-toan")
@CrossOrigin(origins = "http://localhost:5173")
public class HoaDonThanhToanController {
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
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {
        Instant start = (tuNgay != null && !tuNgay.isEmpty()) ? Instant.parse(tuNgay) : null;
        Instant end = (denNgay != null && !denNgay.isEmpty()) ? Instant.parse(denNgay) : null;

        if (key != null) {
            key = key.trim();
        }

        Pageable pageable = PageRequest.of(page, size);
        return hoaDonThanhToanService.searchHoaDon(key,trangThai, start, end, pageable);
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

            System.out.println(username + "FUCK ALL HUMAN");


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
            @RequestParam(required = false) Integer idPhieu) { // 🚨 1. Bổ sung tham số idPhieu (không bắt buộc)

        // 1. Lấy danh sách phiếu đang hoạt động
        List<PhieuDatBan> list = phieuDatBanRepository.findActivePhieuByBanAn(idBanAn);

        if (list == null || list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 🚨 2. LOGIC TÌM ĐÚNG PHIẾU
        PhieuDatBan phieu = null;

        // Nếu Frontend có gửi idPhieu lên -> Ép tìm chính xác phiếu đó
        if (idPhieu != null) {
            phieu = list.stream()
                    .filter(p -> p.getId().equals(idPhieu))
                    .findFirst()
                    .orElse(null);
        }

        // Nếu không truyền idPhieu (Bấm từ sơ đồ bàn) hoặc không tìm thấy -> Lấy cái đầu tiên
        if (phieu == null) {
            phieu = list.get(0);
        }

        // 3. Khởi tạo và mapping thông tin cơ bản sang Response DTO
        PhieuDatBanResponse res = new PhieuDatBanResponse();
        res.setId(phieu.getId());
        res.setMaPhieu(phieu.getMaDatBan());
        res.setThoiGianDat(phieu.getThoiGianDat());
        res.setSoNguoi(phieu.getSoLuongKhach());
        res.setTrangThai(phieu.getTrangThai());

        // 4. Mapping thông tin Khách hàng
        if (phieu.getIdKhachHang() != null) {
            res.setIdKhachHang(phieu.getIdKhachHang().getId());
            res.setTenKhachHang(phieu.getIdKhachHang().getTenKhachHang());
            res.setSdtKhachHang(phieu.getIdKhachHang().getSoDienThoai());
        } else {
            res.setIdKhachHang(null);
            res.setTenKhachHang("Khách vãng lai");
            res.setSdtKhachHang("N/A");
        }

        // 5. Mapping thông tin Bàn ăn và Khu vực hiển thị UI
        if (phieu.getIdBanAn() != null) {
            res.setIdBanAn(phieu.getIdBanAn().getId());
            res.setMaBan(phieu.getIdBanAn().getMaBan());

            if (phieu.getIdBanAn().getIdKhuVuc() != null) {
                res.setTenKhuVuc(phieu.getIdBanAn().getIdKhuVuc().getTenKhuVuc());
                res.setTang(phieu.getIdBanAn().getIdKhuVuc().getTang());
            }
        }

        // 6. LOGIC QUAN TRỌNG: Lấy Hóa đơn... (Giữ nguyên y hệt code cũ của bạn từ đoạn này trở đi)
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieu.getId());

        // Nếu chưa tìm thấy qua Phiếu, thử tìm qua Bàn đang hoạt động (Đảm bảo không bị lọt hóa đơn)
        if (hoaDon == null) {
            hoaDon = hoaDonThanhToanRepository.findActiveBillByBanAn(idBanAn).orElse(null);
        }

        if (hoaDon != null) {
            // 🚨 GÁN ID VÀ THÔNG TIN TÀI CHÍNH CỦA HÓA ĐƠN Ở ĐÂY
            res.setIdHoaDon(hoaDon.getId());
            res.setTongTienChuaGiam(hoaDon.getTongTienChuaGiam() != null ? hoaDon.getTongTienChuaGiam() : BigDecimal.ZERO);
            res.setSoTienDaGiam(hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO);
            res.setTienCoc(hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO);
            res.setTongTienThanhToan(hoaDon.getTongTienThanhToan() != null ? hoaDon.getTongTienThanhToan() : BigDecimal.ZERO);
            res.setVatApDung(hoaDon.getVatApDung() != null ? Double.valueOf(hoaDon.getVatApDung()) : 10.0);

            // Lấy tất cả các món trong chi tiết hóa đơn
            List<ChiTietHoaDon> chiTietHD = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());

            if (chiTietHD != null && !chiTietHD.isEmpty()) {
                List<PhieuDatBanResponse.ChiTietMonResponse> dsMon = chiTietHD.stream().map(item -> {
                    String tenMon = "Không xác định";
                    Integer originalId = null;
                    String type = "";
                    Integer idMonAn = null;
                    Integer idSet = null;

                    // Kiểm tra xem là món lẻ hay set lẩu để lấy thông tin đúng
                    if (item.getIdChiTietMonAn() != null) {
                        tenMon = item.getIdChiTietMonAn().getTenMon();
                        originalId = item.getIdChiTietMonAn().getId();
                        type = "FOOD";
                        idMonAn = originalId;
                    } else if (item.getIdSetLau() != null) {
                        tenMon = item.getIdSetLau().getTenSetLau();
                        originalId = item.getIdSetLau().getId();
                        type = "SET";
                        idSet = originalId;
                    }

                    // Khởi tạo đối tượng rỗng và dùng Setter
                    PhieuDatBanResponse.ChiTietMonResponse chiTietDTO = new PhieuDatBanResponse.ChiTietMonResponse();

                    // Set 8 trường thông tin món
                    chiTietDTO.setId(originalId);
                    chiTietDTO.setTenMon(tenMon);
                    chiTietDTO.setSoLuong(item.getSoLuong());
                    chiTietDTO.setDonGia(item.getDonGiaTaiThoiDiemBan());
                    chiTietDTO.setThanhTien(item.getThanhTien());
                    chiTietDTO.setType(type);
                    chiTietDTO.setIdChiTietMonAn(idMonAn);
                    chiTietDTO.setIdSetLau(idSet);

                    // Setup để thao tác Front-end không lỗi
                    chiTietDTO.setIdChiTietHd(item.getId());
                    chiTietDTO.setTrangThaiMon(item.getTrangThaiMon());

                    return chiTietDTO;
                }).collect(Collectors.toList());

                res.setChiTiet(dsMon);
            } else {
                res.setChiTiet(new ArrayList<>()); // Ép mảng rỗng thay vì null để Vue không bị lỗi
            }
        } else {
            // Hóa đơn null -> Setup giá trị mặc định để Frontend không bị "Undefined"
            res.setTongTienChuaGiam(BigDecimal.ZERO);
            res.setSoTienDaGiam(BigDecimal.ZERO);
            res.setTienCoc(BigDecimal.ZERO);
            res.setTongTienThanhToan(BigDecimal.ZERO);
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
}
