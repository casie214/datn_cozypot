package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.ThamSoHeThong;
import com.example.datn_cozypot_spring_boot.repository.ThamSoHeThongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThamSoHeThongService {

    private final ThamSoHeThongRepository repository;

    public Integer getInteger(String maThamSo, Integer defaultValue) {
        try {
            return repository.findByMaThamSo(maThamSo)
                    .map(p -> Integer.parseInt(p.getGiaTri()))
                    .orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Double getDouble(String maThamSo, Double defaultValue) {
        try {
            return repository.findByMaThamSo(maThamSo)
                    .map(p -> Double.parseDouble(p.getGiaTri()))
                    .orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String maThamSo, String defaultValue) {
        return repository.findByMaThamSo(maThamSo)
                .map(ThamSoHeThong::getGiaTri)
                .orElse(defaultValue);
    }
}