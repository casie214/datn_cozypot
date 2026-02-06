package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
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

    MonAnResponse createMonAn(MonAnRequest request);

    SetLauResponse createSetLau(SetLauRequest request);

    MonAnChiTietResponse createMonAnChiTiet(MonAnChiTietRequest request);

    MonAnChiTietResponse putMonAnChiTiet(int id, MonAnChiTietRequest request);

    MonAnResponse putMonAn(int id, MonAnRequest request);

    SetLauResponse putLau(int id, SetLauRequest request);

    DanhMucResponse putDanhMuc(int id, DanhMucRequest request);

    DanhMucResponse addNewDanhMuc(DanhMucRequest request);

    LoaiLauResponse putLoaiLau(int id, LoaiLauRequest request);

    LoaiLauResponse addNewLoaiLau(LoaiLauRequest request);

    DanhMucChiTietResponse putDanhMucChiTiet(int id, DanhMucChiTietRequest request);

    DanhMucChiTietResponse addNewDanhMucChiTiet(DanhMucChiTietRequest request);

    SetLauResponse findSetLauById(int id);

    MonAnChiTietResponse findChiTietMonAnById(int id);

    void deleteFoodDetailById(int id);

    List<SetLauResponse> findSetLauTop(int metric);

    List<MonAnResponse> findMonAnActive();

    List<DanhMucResponse> findDanhMucActive();

    List<SetLauResponse> findSetLauActive();

    List<DanhMucChiTietResponse> findDanhMucChiTietActive();

    List<LoaiLauResponse> findLoaiSetLauActive();
}
