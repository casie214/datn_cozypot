package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.*;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonAnServiceImplementation implements MonAnService {
    private final MonAnRepository monAnRepository;
    private final MonAnChiTietRepository monAnChiTietRepository;
    private final SetLauRepository setLauRepository;
    private final LoaiLauRepository loaiLauRepository;
    private final DanhMucRepository danhMucRepository;
    private final DanhMucChiTietRepository danhMucChiTietRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<MonAnResponse> findAllMonAn() {
        return monAnRepository.findAll().stream()
                .map(entity -> {
                    MonAnResponse response = modelMapper.map(entity, MonAnResponse.class);
                    if (entity.getIdDanhMucChiTiet() != null) {
                        response.setTenDanhMucChiTiet(entity.getIdDanhMucChiTiet().getTenDanhMucChiTiet());
                        if (entity.getIdDanhMucChiTiet().getIdDanhMuc() != null) {
                            String tenDanhMuc = entity.getIdDanhMucChiTiet().getIdDanhMuc().getTenDanhMuc();
                            response.setTenDanhMuc(tenDanhMuc);
                        }
                    }
                    return response;
                })
                .toList();
    }

    @Override
    public List<SetLauResponse> findAllSetLau() {
        return setLauRepository
                .findAll()
                .stream()
                .map(setLau -> {
                    SetLauResponse response = modelMapper.map(setLau, SetLauResponse.class);
                    return response;
                })
                .toList();
    }

    @Override
    public List<MonAnChiTietResponse> findAllChiTietMonAn() {
        return monAnChiTietRepository
                .findAll()
                .stream()
                .map(
                        chiTietMonAn -> modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class)
                ).toList();
    }

    @Override
    public MonAnResponse findMonAnById(int id) {
        return monAnRepository.findById(id).map(monAnDiKem -> modelMapper.map(monAnDiKem, MonAnResponse.class)).orElse(null);
    }

    @Override
    public List<MonAnChiTietResponse> findChiTietMonAnByMonAnId(int id) {
        return monAnChiTietRepository.findByIdMonAnDiKem_Id(id).stream().map(chiTietMonAn -> modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class)).toList();
    }

    @Override
    public List<DanhMucResponse> findAllDanhMuc() {
        return danhMucRepository
                .findAll()
                .stream()
                .map(danhMuc -> modelMapper.map(danhMuc, DanhMucResponse.class))
                .toList();
    }

    @Override
    public List<DanhMucChiTietResponse> findAllDanhMucChiTiet() {
        return danhMucChiTietRepository
                .findAll()
                .stream()
                .map(danhMucChiTiet -> modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class))
                .toList();
    }

    @Override
    public List<LoaiLauResponse> findAllLoaiLau() {
        return loaiLauRepository
                .findAll()
                .stream()
                .map(loaiSetLau -> modelMapper.map(loaiSetLau, LoaiLauResponse.class))
                .toList();
    }
}
