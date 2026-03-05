package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.dto.profile.NhanVienProfileRequest;
import com.example.datn_cozypot_spring_boot.service.NhanVienService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin(origins = "http://localhost:5173")
public class NhanVienController {

    @Autowired
    private NhanVienService service;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Boolean gioiTinh,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(keyword, trangThai, gioiTinh, tuNgay, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(
            @Valid @ModelAttribute NhanVienRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(service.create(request, file));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @ModelAttribute NhanVienRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(service.update(id, request, file));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.toggleStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam(required = false) Integer excludeId) {
        boolean isExists = service.checkDuplicate(type, value, excludeId);
        return ResponseEntity.ok(Map.of("exists", isExists));
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Boolean gioiTinh,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) List<Integer> listId // Nhận mảng ID từ Frontend
    ) {
        try {
            return service.exportExcel(keyword, trangThai, gioiTinh, tuNgay, listId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export-template")
    public ResponseEntity<Resource> exportTemplate(@RequestParam(required = false) String listId) {
        try {
            // Truyền listId nhận được từ Frontend (ví dụ: "1,2,3") vào Service
            return service.exportTemplate(listId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            service.importExcel(file);
            return ResponseEntity.ok(Map.of("message", "Import danh sách nhân viên thành công!"));
        } catch (RuntimeException e) {
            // Trả về lỗi 400 kèm thông báo chi tiết các dòng bị trùng Email/SĐT
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi xử lý file.");
        }
    }

    @GetMapping("/print-pdf")
    public ResponseEntity<byte[]> printPdf(@RequestParam List<Integer> ids) {
        try {
            // Gọi hàm generatePdf từ service (Hàm đã được dọn dẹp hết lỗi đỏ)
            byte[] pdfContent = service.generatePdf(ids);

            HttpHeaders headers = new HttpHeaders();
            // Thiết lập kiểu file là PDF
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "inline; filename=Danh_Sach_Nhan_Vien.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/scan-qr")
    public ResponseEntity<?> scanQRCode(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ảnh mã QR!");
        }
        try {
            String qrContent = service.decodeQRCode(file);
            if (qrContent == null) {
                return ResponseEntity.status(404).body("Hệ thống không nhận diện được mã QR trong ảnh này.");
            }
            return ResponseEntity.ok(qrContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi xử lý quét ảnh: " + e.getMessage());
        }
    }
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            String currentEmail = authentication.getName();
            return ResponseEntity.ok(service.getProfileByEmail(currentEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể lấy thông tin profile: " + e.getMessage());
        }
    }

    @PutMapping(value = "/update-my-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyProfile(
            Authentication authentication,
            @Valid @ModelAttribute NhanVienProfileRequest request,
            @RequestParam(value = "hinhAnhFile", required = false) MultipartFile file
    ) {
        try {
            String currentEmail = authentication.getName();
            return ResponseEntity.ok(service.updateMyProfile(currentEmail, request, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}