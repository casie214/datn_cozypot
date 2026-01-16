package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoaDonThanhToanService {
    @Autowired
    HoaDonThanhToanRepo hoaDonThanhToanRepo;

    public List<HoaDonThanhToanResponse> getAllHoaDon(){
        return hoaDonThanhToanRepo.getAllHoaDon();
    }



}
