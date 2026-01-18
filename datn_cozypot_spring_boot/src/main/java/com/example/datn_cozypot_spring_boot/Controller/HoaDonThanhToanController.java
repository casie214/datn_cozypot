package com.example.datn_cozypot_spring_boot.Controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.service.ChiTietHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonThanhToanService;
import com.example.datn_cozypot_spring_boot.service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/hoa-don-thanh-toan")
@CrossOrigin(origins = "http://localhost:5173")
public class HoaDonThanhToanController {
    @Autowired
    HoaDonThanhToanService hoaDonThanhToanService;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @Autowired
    LichSuHoaDonService lichSuHoaDonService;

    @GetMapping("/get-all")
    public List<HoaDonThanhToanResponse> getAll(){
        return hoaDonThanhToanService.getAllHoaDon();
    }

    @GetMapping("/search")
    public List<HoaDonThanhToanResponse> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {

        Instant start = (tuNgay != null && !tuNgay.isEmpty()) ? Instant.parse(tuNgay) : null;
        Instant end = (denNgay != null && !denNgay.isEmpty()) ? Instant.parse(denNgay) : null;

        return hoaDonThanhToanService.searchHoaDon(key,trangThai, start, end);
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
}
