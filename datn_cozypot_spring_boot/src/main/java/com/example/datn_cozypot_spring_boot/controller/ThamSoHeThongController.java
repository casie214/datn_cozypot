package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.service.ThamSoHeThongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tham-so-he-thong")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ThamSoHeThongController {

    private final ThamSoHeThongService service;

    @GetMapping("/get-all-system")
    public Map<String, Object> getGlobalParams() {

        Map<String, Object> params = new HashMap<>();

        params.put("VAT", service.getDouble("VAT", 10.0));
        params.put("MIN_RESERVE", service.getInteger("MIN_RESERVE", 30));
        params.put("MAX_HOLD_TIME", service.getInteger("MAX_HOLD_TIME", 15));
        params.put("CANCEL_LIMIT_HOURS", service.getDouble("CANCEL_LIMIT_HOURS", 2.0));

        return params;
    }
}