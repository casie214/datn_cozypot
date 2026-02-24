package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanRequest;
import com.example.datn_cozypot_spring_boot.service.BookingService.BookingService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest/reservation")
@RequiredArgsConstructor
public class PhieuDatBanController {

    private final BookingService datBanService;

    @PostMapping("/create")
    public ResponseEntity<?> createReservation(@RequestBody PhieuDatBanRequest request) {
        try {
            String result = datBanService.taoDonDatBan(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
