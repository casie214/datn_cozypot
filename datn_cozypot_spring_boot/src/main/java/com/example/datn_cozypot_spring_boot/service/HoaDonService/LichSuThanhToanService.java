package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.repository.LichSuHoaDonRepo;
import com.example.datn_cozypot_spring_boot.repository.LichSuThanhToanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LichSuThanhToanService {
    @Autowired
    LichSuThanhToanRepo lichSuThanhToanRepo;

    public List<LichSuThanhToanResponse> getAllLichSuThanhToan(Integer idHoaDon){
        return lichSuThanhToanRepo.getLichSuByIdHoaDon(idHoaDon);
    }
}
