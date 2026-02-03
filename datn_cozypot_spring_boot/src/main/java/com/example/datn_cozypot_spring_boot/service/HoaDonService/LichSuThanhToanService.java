package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.repository.LichSuThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LichSuThanhToanService {
    @Autowired
    LichSuThanhToanRepository lichSuThanhToanRepository;

    public List<LichSuThanhToanResponse> getAllLichSuThanhToan(Integer idHoaDon){
        return lichSuThanhToanRepository.getLichSuByIdHoaDon(idHoaDon);
    }
}
