package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.ThamSoHeThong;
import com.example.datn_cozypot_spring_boot.repository.ThamSoHeThongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThamSoHeThongService {

    private final ThamSoHeThongRepository repository;

    public List<ThamSoHeThong> getAll() {
        return repository.findAll();
    }

    public ThamSoHeThong update(Integer id, ThamSoHeThong request) {
        ThamSoHeThong ts = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        ts.setTenThamSo(request.getTenThamSo());
        ts.setGiaTri(request.getGiaTri());
        ts.setMoTa(request.getMoTa());
        ts.setKieuDuLieu(request.getKieuDuLieu());

        return repository.save(ts);
    }

    public void updateStatus(String maThamSo, Integer trangThai) {
        ThamSoHeThong ts = repository.findByMaThamSo(maThamSo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        ts.setTrangThai(trangThai);
        repository.save(ts);
    }

    // 🔥 Method dùng cho hệ thống
    public Double getDouble(String ma, Double defaultValue) {
        return repository.findByMaThamSo(ma)
                .filter(t -> t.getTrangThai() == 1)
                .map(t -> Double.parseDouble(t.getGiaTri()))
                .orElse(defaultValue);
    }

    public Integer getInteger(String ma, Integer defaultValue) {
        return repository.findByMaThamSo(ma)
                .filter(t -> t.getTrangThai() == 1)
                .map(t -> Integer.parseInt(t.getGiaTri()))
                .orElse(defaultValue);
    }

    public ThamSoHeThong updatePartial(Integer id, Map<String, Object> updates) {

        ThamSoHeThong ts = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số"));

        if (updates.containsKey("tenThamSo")) {
            ts.setTenThamSo((String) updates.get("tenThamSo"));
        }

        if (updates.containsKey("giaTri")) {
            ts.setGiaTri((String) updates.get("giaTri"));
        }

        if (updates.containsKey("moTa")) {
            ts.setMoTa((String) updates.get("moTa"));
        }

        if (updates.containsKey("kieuDuLieu")) {
            ts.setKieuDuLieu((String) updates.get("kieuDuLieu"));
        }

        return repository.save(ts);
    }
}