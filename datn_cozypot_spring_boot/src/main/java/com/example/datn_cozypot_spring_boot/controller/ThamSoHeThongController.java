package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.entity.ThamSoHeThong;
import com.example.datn_cozypot_spring_boot.repository.ThamSoHeThongRepository;
import com.example.datn_cozypot_spring_boot.service.ThamSoHeThongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tham-so-he-thong")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ThamSoHeThongController {

    private final ThamSoHeThongService service;
    private final ThamSoHeThongRepository thamSoHeThongRepository;

    @GetMapping
    public List<ThamSoHeThong> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}")
    public ThamSoHeThong update(
            @PathVariable Integer id,
            @RequestBody ThamSoHeThong request) {

        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    public ThamSoHeThong updatePartial(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {

        return service.updatePartial(id, updates);
    }

    @PutMapping("/update-status/{maThamSo}")
    public void updateStatus(
            @PathVariable String maThamSo,
            @RequestBody Map<String, Integer> body) {

        service.updateStatus(maThamSo, body.get("trangThai"));
    }

    @GetMapping("/get-all-system")
    public List<ThamSoHeThong> getAllSystem() {
        return service.getAll();
    }

    @GetMapping("/all-map")
    public ResponseEntity<Map<String, String>> getAllThamSoMap() {
        List<ThamSoHeThong> list = thamSoHeThongRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (ThamSoHeThong ts : list) {
            map.put(ts.getMaThamSo(), ts.getGiaTri());
        }
        return ResponseEntity.ok(map);
    }
}