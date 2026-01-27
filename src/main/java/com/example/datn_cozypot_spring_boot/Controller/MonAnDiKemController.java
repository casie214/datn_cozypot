package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.MonAnDiKemDTO;
import com.example.datn_cozypot_spring_boot.dto.SetLauDTO;
import com.example.datn_cozypot_spring_boot.service.MonAnDiKemService;
import com.example.datn_cozypot_spring_boot.service.SetLauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mon-an-di-kem")
public class MonAnDiKemController {
    @Autowired
    private MonAnDiKemService monAnDiKemService;

    @GetMapping("/active")
    public ResponseEntity<List<MonAnDiKemDTO>> getActiveSets() {
        return ResponseEntity.ok(monAnDiKemService.getActiveMonAns());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<MonAnDiKemDTO>> getAllSets() { // Sửa SetLauDTO thành MonAnDiKemDTO
        return ResponseEntity.ok(monAnDiKemService.getActiveMonAns());
    }
}
