package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.entity.ThamSoHeThong;
import com.example.datn_cozypot_spring_boot.repository.ThamSoHeThongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tham-so-he-thong")
@CrossOrigin(origins = "http://localhost:5173")
public class ThamSoHeThongController {
    @Autowired
    ThamSoHeThongRepository thamSoRepo;

    @GetMapping("/get-all-system")
    public Map<String, Object> getGlobalParams() {
        Map<String, Object> params = new HashMap<>();

        // Lấy VAT (Chuyển String sang Double)
        ThamSoHeThong vatParam = thamSoRepo.findByMaThamSo("VAT").orElse(null);
        if (vatParam != null) {
            try {
                params.put("VAT", Double.parseDouble(vatParam.getGiaTri()));
            } catch (NumberFormatException e) {
                params.put("VAT", 10.0); // Mặc định nếu DB nhập sai
            }
        } else {
            params.put("VAT", 0.0); // Không tìm thấy thì 0%
        }

        // Lấy thời gian chờ (Chuyển String sang Int)
        ThamSoHeThong timeParam = thamSoRepo.findByMaThamSo("MIN_RESERVE").orElse(null);
        if (timeParam != null) {
            try {
                params.put("MIN_RESERVE", Integer.parseInt(timeParam.getGiaTri()));
            } catch (NumberFormatException e) {
                params.put("MIN_RESERVE", 30);
            }
        }

        return params;
    }
}
