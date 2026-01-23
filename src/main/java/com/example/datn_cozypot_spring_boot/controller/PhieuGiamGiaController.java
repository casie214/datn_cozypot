package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaDTO;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaResponseDTO;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = "http://localhost:5173")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService service;

    // ============= GET ALL (PHÂN TRANG + TÌM KIẾM) =============

    @GetMapping("/search")
    public ResponseEntity<?> searchVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer doiTuong,
            @RequestParam(required = false) Integer loaiGiamGia,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String ngayBatDau,
            @RequestParam(required = false) String ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(service.getAll(
                keyword, doiTuong, loaiGiamGia, trangThai, ngayBatDau, ngayKetThuc, page, size
        ));
    }

    // ============= GET BY ID =============

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    // ============= CREATE =============

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PhieuGiamGiaDTO dto) {
        service.create(dto);
        return ResponseEntity.ok("Thêm phiếu giảm giá thành công");
    }











    // ============= UPDATE =============

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PhieuGiamGiaDTO dto
    ) {
        try {
            PhieuGiamGia updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // ============= DELETE =============

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("Xóa thành công phiếu giảm giá ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}
