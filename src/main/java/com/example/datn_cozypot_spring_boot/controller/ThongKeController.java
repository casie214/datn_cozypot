package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeNgayDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeThanhToanDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeTongDTO;
import com.example.datn_cozypot_spring_boot.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;


    // Thống kê tổng
    @GetMapping("/tong")
    public ResponseEntity<?> thongKeTong(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        ThongKeTongDTO result = thongKeService.thongKeTong(from, to);

        return ResponseEntity.ok(result);
    }


    // Thống kê theo hình thức thanh toán
    @GetMapping("/thanh-toan")
    public ResponseEntity<?> thongKeThanhToan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        List<ThongKeThanhToanDTO> result =
                thongKeService.thongKeThanhToan(from, to);

        return ResponseEntity.ok(result);
    }


    // Thống kê theo ngày
    @GetMapping("/theo-ngay")
    public ResponseEntity<?> thongKeTheoNgay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        List<ThongKeNgayDTO> result =
                thongKeService.thongKeTheoNgay(from, to);

        return ResponseEntity.ok(result);
    }

}

