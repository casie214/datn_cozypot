package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.service.LichSuDatBanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lich-su-dat-ban")
@CrossOrigin(origins = "http://localhost:5173")
public class LichSuDatBanController {

    private final LichSuDatBanService lichSuDatBanService;

    @GetMapping("/tra-cuu")
    public ResponseEntity<?> traCuuKhachVangLai(@RequestParam String maPhieu, @RequestParam String sdt) {
        try {
            Map<String, Object> data = lichSuDatBanService.traCuuKhachVangLai(maPhieu, sdt);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/chi-tiet/{maPhieu}")
    public ResponseEntity<?> xemChiTietKhachDangNhap(@PathVariable String maPhieu) {
        try {
            Map<String, Object> data = lichSuDatBanService.xemChiTietChoKhachDangNhap(maPhieu);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ca-nhan")
    public ResponseEntity<?> layLichSuCaNhan() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            return ResponseEntity.ok(lichSuDatBanService.layLichSuCaNhan(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi xác thực người dùng. Vui lòng đăng nhập lại!"));
        }
    }

    @PutMapping("/huy-don/{idPhieu}")
    public ResponseEntity<?> khachTuHuyDon(
            @PathVariable Integer idPhieu,
            @RequestBody Map<String, String> body) {
        try {
            String lyDo = body.getOrDefault("lyDo", "Khách hàng tự hủy trên website");

            lichSuDatBanService.khachTuHuyDon(idPhieu, lyDo);

            return ResponseEntity.ok(Map.of("message", "Hủy phiếu đặt bàn thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}