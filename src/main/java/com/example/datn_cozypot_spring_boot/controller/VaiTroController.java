package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.VaiTroRequest;
import com.example.datn_cozypot_spring_boot.service.VaiTroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vai-tro")
@CrossOrigin("*")
public class VaiTroController {
    @Autowired
    private VaiTroService service;

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(keyword, page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveRoles() {
        return ResponseEntity.ok(service.getActiveRoles());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody VaiTroRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody VaiTroRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }
}