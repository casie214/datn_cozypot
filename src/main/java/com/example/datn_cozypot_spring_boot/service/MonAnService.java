package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongItemRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViRequest;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.DinhLuong;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MonAnService {
    List<DanhMucResponse> getAllDanhMuc();
    DanhMucResponse createDanhMuc(DanhMucRequest request);
    DanhMucResponse updateDanhMuc(Integer id, DanhMucRequest request);
    void deleteDanhMuc(Integer id);


    // --- Món Ăn (DanhMucChiTiet) ---
    List<MonAnResponse> getAllMonAn();
    MonAnResponse getMonAnById(Integer id);
    MonAnResponse createMonAn(MonAnRequest request);
    MonAnResponse updateMonAn(Integer id, MonAnRequest request);
    void deleteMonAn(Integer id);

    // --- Loại Set Lẩu ---
    List<LoaiLauResponse> getAllLoaiSetLau();
    LoaiLauResponse createLoaiSetLau(LoaiLauRequest request);
    LoaiLauResponse updateLoaiSetLau(Integer id, LoaiLauRequest request);

    // --- Set Lẩu ---
    List<SetLauResponse> getAllSetLau();
    SetLauResponse getSetLauById(Integer id);
    SetLauResponse createSetLau(SetLauRequest request);
    SetLauResponse updateSetLau(Integer id, SetLauRequest request);

    List<DanhMucResponse> findDanhMucActive();

    List<LoaiLauResponse> findLoaiSetLauActive();

    List<SetLauResponse> findSetLauActive();

    List<MonAnResponse> findMonAnActive();

    List<SetLauResponse> findSetLauTop(int metric);


    List<DonViResponse> getAllDonVi();

    DonViResponse createDonVi(DonViRequest request);

    DonViResponse updateDonVi(Integer id, DonViRequest request);

    List<DonViResponse> getDonViByCategoryId(Integer id);

    void updateDinhLuongSingle(Integer id, DinhLuongItemRequest req);
}
