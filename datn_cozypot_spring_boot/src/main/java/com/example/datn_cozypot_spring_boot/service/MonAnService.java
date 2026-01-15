package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MonAnService {
    List<MonAnResponse> findAllMonAn();

    List<SetLauResponse> findAllSetLau();

    List<MonAnChiTietResponse> findAllChiTietMonAn();

    MonAnResponse findMonAnById(int id);

    List<MonAnChiTietResponse> findChiTietMonAnByMonAnId(int id);

    List<DanhMucResponse> findAllDanhMuc();

    List<DanhMucChiTietResponse> findAllDanhMucChiTiet();

    List<LoaiLauResponse> findAllLoaiLau();
}
