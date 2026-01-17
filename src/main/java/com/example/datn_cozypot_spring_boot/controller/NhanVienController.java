package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin(origins = "http://localhost:5173")
public class NhanVienController {
    @Autowired private NhanVienService service;

    // 1. Lấy danh sách mặc định (Có phân trang)
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Luôn dùng getAll để có phân trang và sắp xếp đúng như giao diện
        return ResponseEntity.ok(service.getAll(keyword, trangThai, tuNgay, page, size));
    }

    // 2. Xem chi tiết nhân viên
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    // 3. Thêm mới nhân viên
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody NhanVienRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Cập nhật nhân viên (Chức năng Sửa - bao gồm cả đổi trạng thái)
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody NhanVienRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.toggleStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}