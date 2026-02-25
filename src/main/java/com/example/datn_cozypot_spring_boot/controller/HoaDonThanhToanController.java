package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.ChiTietHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.HoaDonThanhToanService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuThanhToanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            @RequestParam(required = false) Integer trangThaiHoanTien,
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
        return hoaDonThanhToanService.searchHoaDon(key,trangThai,trangThaiHoanTien, start, end, pageable);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<HoaDonThanhToanResponse> getById(@PathVariable Integer id) {
        HoaDonThanhToanResponse response = hoaDonThanhToanService.getHoaDonById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
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
    public ResponseEntity<PhieuDatBanResponse> findActivePhieuByBanAn(@PathVariable Integer idBanAn) {
        // 1. Lấy danh sách phiếu đang hoạt động từ Repository
        List<PhieuDatBan> list = phieuDatBanRepository.findActivePhieuByBanAn(idBanAn);

        if (list == null || list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 2. Lấy phiếu mới nhất
        PhieuDatBan phieu = list.get(0);

        // 3. Mapping sang DTO
        PhieuDatBanResponse res = new PhieuDatBanResponse();
        res.setId(phieu.getId());

        // ĐOẠN LẤY MÃ PHIẾU: Bạn hãy kiểm tra lại Entity, thường là một trong các tên sau:
        // phieu.getMaPhieuDatBan() hoặc phieu.getMaDatBan() hoặc phieu.getMaPhieu()
        res.setMaPhieu(phieu.getMaDatBan());

        res.setThoiGianDat(phieu.getThoiGianDat());
        res.setSoNguoi(phieu.getIdBanAn().getSoNguoiToiDa());
        res.setTrangThai(phieu.getTrangThai());

        // 4. Mapping thông tin Khách hàng (Thêm kiểm tra null để an toàn)
        if (phieu.getIdKhachHang() != null) {
            res.setTenKhachHang(phieu.getIdKhachHang().getTenKhachHang());
            res.setSdtKhachHang(phieu.getIdKhachHang().getSoDienThoai());
        } else {
            // Nếu là khách vãng lai hoặc không có data khách hàng
            res.setTenKhachHang("Khách vãng lai");
            res.setSdtKhachHang("N/A");
        }

        // 5. Mapping thông tin Bàn ăn và Khu vực
        if (phieu.getIdBanAn() != null) {
            res.setIdBanAn(phieu.getIdBanAn().getId());
            res.setMaBan(phieu.getIdBanAn().getMaBan());

            if (phieu.getIdBanAn().getIdKhuVuc() != null) {
                res.setTenKhuVuc(phieu.getIdBanAn().getIdKhuVuc().getTenKhuVuc());
                res.setTang(phieu.getIdBanAn().getIdKhuVuc().getTang());
            }
        }

        return ResponseEntity.ok(res);
    }
}
