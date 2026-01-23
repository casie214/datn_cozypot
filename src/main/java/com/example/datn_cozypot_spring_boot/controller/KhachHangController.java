package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.KhachHangRequest;
import com.example.datn_cozypot_spring_boot.dto.KhachHangResponse;
import com.example.datn_cozypot_spring_boot.service.KhachHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend gọi API
public class KhachHangController {

    @Autowired
    private KhachHangService service;

    @GetMapping
    public ResponseEntity<Page<KhachHangResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Gọi service xử lý tìm kiếm theo keyword (mã, tên, sdt, email) và ngày tạo
        return ResponseEntity.ok(service.getAll(keyword, trangThai, tuNgay, page, size));
    }

    // Xem chi tiết khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.getDetail(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Thêm mới khách hàng
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid KhachHangRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            // Trả về thông báo lỗi nếu trùng SĐT, Email...
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật khách hàng
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable @Valid Integer id, @RequestBody KhachHangRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //Khóa/ mở khóa
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.toggleStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Thêm phương thức này để xử lý lỗi đỏ trong Console của bạn
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCustomers() {
        // Giả sử service của bạn có hàm lấy khách hàng đang hoạt động để tặng Voucher
        return ResponseEntity.ok(service.findAllActive());
    }

}