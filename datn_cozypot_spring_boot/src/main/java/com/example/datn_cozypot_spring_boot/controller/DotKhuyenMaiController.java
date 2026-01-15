package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.DotKhuyenMaiDTO;
import com.example.datn_cozypot_spring_boot.service.DotKhuyenMaiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dot-khuyen-mai")

public class DotKhuyenMaiController {
    @Autowired
    private DotKhuyenMaiService dotKhuyenMaiService;

    @GetMapping("/get-all")
    public ResponseEntity<List<DotKhuyenMaiDTO>> getAll() {
        return ResponseEntity.ok(dotKhuyenMaiService.getAll());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody DotKhuyenMaiDTO dto) {
        try {
            return ResponseEntity.ok(dotKhuyenMaiService.create(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi thêm mới: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody DotKhuyenMaiDTO dto
    ) {
        try {
            DotKhuyenMaiDTO result = dotKhuyenMaiService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DotKhuyenMaiDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type
    ) {
        return ResponseEntity.ok(
                dotKhuyenMaiService.search(keyword, status, type)
        );
    }
}
