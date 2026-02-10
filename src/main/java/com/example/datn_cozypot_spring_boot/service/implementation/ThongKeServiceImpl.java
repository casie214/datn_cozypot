package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeNgayDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeThanhToanDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeTongDTO;
import com.example.datn_cozypot_spring_boot.repository.ThongKeRepository;
import com.example.datn_cozypot_spring_boot.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThongKeServiceImpl implements ThongKeService {

    @Autowired
    private ThongKeRepository thongKeRepository;


    @Override
    public ThongKeTongDTO thongKeTong(LocalDate from, LocalDate to) {
        return thongKeRepository.thongKeTong(from, to);
    }

    @Override
    public List<ThongKeThanhToanDTO> thongKeThanhToan(LocalDate from, LocalDate to) {
        return thongKeRepository.thongKeThanhToan(from, to);
    }


    @Override
    public List<ThongKeNgayDTO> thongKeTheoNgay(LocalDate from, LocalDate to) {
        return thongKeRepository.thongKeTheoNgay(from, to);
    }
}

