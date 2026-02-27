package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.thongKe.KenhDatResponse;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeDoanhThuDTO;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*") // Để Vue.js có thể gọi được API từ port khác
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;
    @GetMapping("/doanh-thu")
    public ThongKeDoanhThuDTO layDoanhThu(@RequestParam(name = "loai", defaultValue = "Tháng này") String loai) {
        // Gọi hàm xử lý chung và truyền tham số "loai" vào
        return thongKeService.layThongKeTheoLoai(loai);
    }

    @GetMapping("/trang-thai-don-hang")
    public ResponseEntity<?> layTrangThaiDonHang() {
        return ResponseEntity.ok(thongKeService.layTrangThaiDonHang());
    }

    @GetMapping("/kenh-dat")
    public ResponseEntity<List<KenhDatResponse>> thongKeKenhDat() {
        return ResponseEntity.ok(thongKeService.thongKeKenhDat());
    }


}