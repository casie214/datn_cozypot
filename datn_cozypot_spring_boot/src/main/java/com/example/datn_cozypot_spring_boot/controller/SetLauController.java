package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.SetLauDTO;
import com.example.datn_cozypot_spring_boot.service.SetLauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/set-lau")
@CrossOrigin(origins = "http://localhost:5173")
public class SetLauController {

    @Autowired
    private SetLauService setLauService;

    @GetMapping("/active")
    public ResponseEntity<List<SetLauDTO>> getActiveSets() {
        return ResponseEntity.ok(setLauService.getActiveSets());
    }

    // THÊM API NÀY ĐỂ KHỚP VỚI FRONTEND
    @GetMapping("/get-all")
    public ResponseEntity<List<SetLauDTO>> getAllSets() {
        // Bạn có thể gọi service lấy tất cả hoặc lấy active tùy nhu cầu
        return ResponseEntity.ok(setLauService.getActiveSets());
    }
}