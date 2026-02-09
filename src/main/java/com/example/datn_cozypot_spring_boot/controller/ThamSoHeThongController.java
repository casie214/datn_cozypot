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

        // 1. VAT
        params.put("VAT", getParamValue("VAT", 10.0)); // Mặc định 10%

        // 2. Thời gian đặt trước tối thiểu (phút)
        params.put("MIN_RESERVE", getParamValue("MIN_RESERVE", 30));

        // 3. Thời gian giữ bàn (phút) - MỚI
        params.put("MAX_HOLD_TIME", getParamValue("MAX_HOLD_TIME", 15)); // Mặc định 15p

        // 4. Giới hạn hủy trước giờ (giờ) - MỚI
        params.put("CANCEL_LIMIT_HOURS", getParamValue("CANCEL_LIMIT_HOURS", 2.0)); // Mặc định 2h

        return params;
    }

    // Hàm phụ trợ để lấy giá trị an toàn (đỡ phải try-catch nhiều lần)
    private <T> T getParamValue(String maThamSo, T defaultValue) {
        ThamSoHeThong param = thamSoRepo.findByMaThamSo(maThamSo).orElse(null);
        if (param == null) return defaultValue;

        try {
            String val = param.getGiaTri();
            if (defaultValue instanceof Integer) {
                return (T) Integer.valueOf(val);
            } else if (defaultValue instanceof Double) {
                return (T) Double.valueOf(val);
            }
            return (T) val;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}