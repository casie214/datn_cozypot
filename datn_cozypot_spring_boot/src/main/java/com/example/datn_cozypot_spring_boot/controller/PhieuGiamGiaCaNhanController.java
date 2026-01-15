package com.example.datn_cozypot_spring_boot.controller;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaCaNhanDTO;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGiaCaNhan;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaCaNhanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phieu-giam-gia-ca-nhan")
public class PhieuGiamGiaCaNhanController {

    @Autowired
    private PhieuGiamGiaCaNhanService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<PhieuGiamGiaCaNhanDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PhieuGiamGiaCaNhanDTO>> search(
            @RequestParam(required = false) String tenKH,
            @RequestParam(required = false) Integer status
    ) {
        return ResponseEntity.ok(service.search(tenKH, status));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody PhieuGiamGiaCaNhanDTO dto) {
        try {
            PhieuGiamGiaCaNhan result = service.create(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PhieuGiamGiaCaNhanDTO dto
    ) {
        try {
            PhieuGiamGiaCaNhan result = service.update(id, dto);

            if (result == null) {
                return ResponseEntity.badRequest()
                        .body("Không tìm thấy phiếu cá nhân id: " + id);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
