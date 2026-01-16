package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChiTietHoaDonService {
    @Autowired
    ChiTietHoaDonRepo chiTietHoaDonRepo;

    public List<ChiTietHoaDonResponse> getAllChiTietHoaDon(Integer idHoaDon){
        return chiTietHoaDonRepo.findChiTietByHoaDonId(idHoaDon);
    }
}
