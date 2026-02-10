package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.service.NhanVienService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin(origins = "http://localhost:5173") // Đảm bảo đúng port của Vue
public class NhanVienController {

    @Autowired
    private NhanVienService service;

    // 1. Lấy danh sách (Phân trang + Tìm kiếm)
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(keyword, trangThai, tuNgay, page, size));
    }

    // 2. Xem chi tiết nhân viên
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    // 3. Thêm mới nhân viên
    // SỬA: Dùng @ModelAttribute để nhận FormData, consumes multipart/form-data
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(
            @Valid @ModelAttribute NhanVienRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(service.create(request, file));
    }


    // 4. Cập nhật nhân viên
    // SỬA: Dùng @ModelAttribute và nhận file kèm theo
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @ModelAttribute NhanVienRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(service.update(id, request, file));
    }


    // 5. Đổi trạng thái nhanh (Ngừng hoạt động / Đang làm việc)
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.toggleStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. Kiểm tra trùng dữ liệu (CCCD, Email, SĐT, Username)
    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam(required = false) Integer excludeId) {
        boolean isExists = service.checkDuplicate(type, value, excludeId);
        return ResponseEntity.ok(Map.of("exists", isExists));
    }

    // 7. Xuất file Excel
    @GetMapping("/export")
    public ResponseEntity<org.springframework.core.io.Resource> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay) {
        try {
            // Gọi service để tạo file Excel (trả về InputStreamResource)
            return service.exportExcel(keyword, trangThai, tuNgay);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}