package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietHoaDon;
import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepo;
import com.example.datn_cozypot_spring_boot.repository.SetLauChiTietRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChiTietHoaDonService {
    @Autowired
    ChiTietHoaDonRepo chiTietHoaDonRepo;

    @Autowired
    SetLauChiTietRepository setLauChiTietRepository;

    public List<ChiTietHoaDonResponse> getAllChiTietHoaDon(Integer idHoaDon){
        return chiTietHoaDonRepo.findChiTietByHoaDonId(idHoaDon);
    }

    public List<ChiTietSetLauResponse> getChiTietSetLau(Integer idSetLau){
        return setLauChiTietRepository.findChiTietBySetId(idSetLau);
    }

    @Transactional
    public void updateToServed(Integer idChiTietHD) {
        ChiTietHoaDon ct = chiTietHoaDonRepo.findById(idChiTietHD)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn!"));
        if (ct.getTrangThaiMon() == 1) {
            ct.setTrangThaiMon(2);
            chiTietHoaDonRepo.save(ct);
        }
    }

    // Cập nhật TẤT CẢ món của 1 hóa đơn sang "Đã lên"
    @Transactional
    public void updateAllToServed(Integer idHoaDon) {
        List<ChiTietHoaDon> dsMon = chiTietHoaDonRepo.findByIdHoaDon(idHoaDon);
        for (ChiTietHoaDon ct : dsMon) {
            if (ct.getTrangThaiMon() == 1) {
                ct.setTrangThaiMon(2);
            }
        }
        chiTietHoaDonRepo.saveAll(dsMon);
    }

    // Kiểm tra xem hóa đơn đã có món nào lên bàn (Trạng thái 2) chưa
    public boolean hasAnyDishServed(Integer idHoaDon) {
        return chiTietHoaDonRepo.existsByIdHoaDonAndTrangThaiMon(idHoaDon, 2);
    }
}