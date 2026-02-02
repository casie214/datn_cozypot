package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.KhachHangRequest;
import com.example.datn_cozypot_spring_boot.service.KhachHangService;
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
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "http://localhost:5173")
public class KhachHangController {

    @Autowired
    private KhachHangService service;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(keyword, trangThai, tuNgay, page, size));
    }

    // 2. Xem chi tiết khách hàng
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.getDetail(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Thêm mới khách hàng
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(
            @Valid @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        try {
            return ResponseEntity.ok(service.create(request, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Cập nhật khách hàng
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        try {
            return ResponseEntity.ok(service.update(id, request, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Đổi trạng thái nhanh (Đảo trạng thái hoạt động)
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.toggleStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. Kiểm tra trùng dữ liệu (Email, SĐT, Tên đăng nhập)
    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam(required = false) Integer excludeId) {
        boolean isExists = service.checkDuplicate(type, value, excludeId);
        return ResponseEntity.ok(Map.of("exists", isExists));
    }
    // 7. Xuất file Excel khách hàng
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay) {
        try {
            // Gọi service để lấy mảng byte của file Excel
            byte[] data = service.exportExcel(keyword, trangThai, tuNgay);

            // Tạo tên file kèm thời gian hiện tại cho chuyên nghiệp
            String fileName = "DS_KhachHang_" + LocalDate.now() + ".xlsx";

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}