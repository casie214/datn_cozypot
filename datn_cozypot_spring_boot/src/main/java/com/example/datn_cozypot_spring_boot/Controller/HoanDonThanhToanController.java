package com.example.datn_cozypot_spring_boot.Controller;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.service.ChiTietHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don-thanh-toan")
@CrossOrigin(origins = "http://localhost:5173")
public class HoanDonThanhToanController {
    @Autowired
    HoaDonThanhToanService hoaDonThanhToanService;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @GetMapping("/get-all")
    public List<HoaDonThanhToanResponse> getAll(){
        return hoaDonThanhToanService.getAllHoaDon();
    }

    @GetMapping("/chi-tiet-hoa-don/{idHoaDon}")
    public List<ChiTietHoaDonResponse> getAllChiTiet(@PathVariable Integer idHoaDon){
        return chiTietHoaDonService.getAllChiTietHoaDon(idHoaDon);
    }
}
