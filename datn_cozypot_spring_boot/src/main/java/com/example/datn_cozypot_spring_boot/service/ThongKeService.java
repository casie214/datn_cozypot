package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeNgayDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeThanhToanDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeTongDTO;
import com.example.datn_cozypot_spring_boot.repository.ThongKeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

public interface ThongKeService {

    ThongKeTongDTO thongKeTong(LocalDate from, LocalDate to);

    List<ThongKeThanhToanDTO> thongKeThanhToan(LocalDate from, LocalDate to);

    List<ThongKeNgayDTO> thongKeTheoNgay(LocalDate from, LocalDate to);

}
