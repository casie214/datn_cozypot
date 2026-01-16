package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietMonAn;
import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import com.example.datn_cozypot_spring_boot.entity.SetLau;
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

    @Override
    public MonAnResponse createMonAn(MonAnRequest request) {
        MonAnDiKem monAn = modelMapper.map(request, MonAnDiKem.class);
        monAn.setId(null);
        monAnRepository.save(monAn);
        MonAnResponse monAnResponse = modelMapper.map(monAn, MonAnResponse.class);
        DanhMucChiTietResponse danhMucChiTietResponse = danhMucChiTietRepository.findById(monAnResponse.getIdDanhMucChiTiet()).map(danhMucChiTiet -> modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class)).orElse(null);
        monAnResponse.setTenDanhMucChiTiet(danhMucChiTietResponse.getTenDanhMucChiTiet());
        monAnResponse.setTenDanhMuc(danhMucChiTietResponse.getTenDanhMuc());
        return monAnResponse;
    }

    @Override
    public SetLauResponse createSetLau(SetLauRequest request) {
        SetLau setLau = modelMapper.map(request, SetLau.class);
        setLau.setId(null);
        setLauRepository.save(setLau);
        SetLauResponse setLauResponse = modelMapper.map(setLau, SetLauResponse.class);
        LoaiLauResponse loaiLauResponse = loaiLauRepository.findById(setLauResponse.getIdLoaiSet()).map(loaiSetLau -> modelMapper.map(loaiSetLau, LoaiLauResponse.class)).orElse(null);
        setLauResponse.setTenLoaiSet(loaiLauResponse.getTenLoaiSet());
        return setLauResponse;
    }

    @Override
    public MonAnChiTietResponse createMonAnChiTiet(MonAnChiTietRequest request) {
        ChiTietMonAn chiTietMonAn = modelMapper.map(request, ChiTietMonAn.class);
        chiTietMonAn.setId(null);
        monAnChiTietRepository.save(chiTietMonAn);
        MonAnChiTietResponse chiTietResponse = modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class);
        return chiTietResponse;
    }

    @Override
    public MonAnChiTietResponse putMonAnChiTiet(int id, MonAnChiTietRequest request) {
        return monAnChiTietRepository.findById(id)
                .map(chiTietMonAn -> {
                    chiTietMonAn.setTenChiTietMonAn(request.getTenChiTietMonAn());
                    chiTietMonAn.setIdMonAnDiKem(monAnRepository.findById(request.getIdMonAnDiKem()).get());
                    chiTietMonAn.setGiaBan(request.getGiaBan());
                    chiTietMonAn.setGiaVon(request.getGiaVon());
                    chiTietMonAn.setKichCo(request.getKichCo());
                    chiTietMonAn.setDonVi(request.getDonVi());
                    chiTietMonAn.setTrangThai(request.getTrangThai());

                    monAnChiTietRepository.save(chiTietMonAn);
                    return modelMapper.map(chiTietMonAn, MonAnChiTietResponse.class);
                })
                .orElse(null);
    }

    @Override
    public MonAnResponse putMonAn(int id, MonAnRequest request) {
        return monAnRepository.findById(id)
                .map(monAnDiKem -> {
                    monAnDiKem.setTenMonAn(request.getTenMonAn());
                    monAnDiKem.setGiaBan(request.getGiaBan());
                    monAnDiKem.setIdDanhMucChiTiet(danhMucChiTietRepository.findById(request.getIdDanhMucChiTiet()).get());
                    monAnDiKem.setTrangThaiKinhDoanh(request.getTrangThaiKinhDoanh());
                    monAnDiKem.setMoTa(request.getMoTa());

                    monAnRepository.save(monAnDiKem);
                    return modelMapper.map(monAnDiKem, MonAnResponse.class);
                })
                .orElse(null);
    }

    @Override
    public SetLauResponse putLau(int id, SetLauRequest request) {
        return setLauRepository.findById(id)
                .map(setLau -> {
                    setLau.setTenSetLau(request.getTenSetLau());
                    setLau.setGiaBan(request.getGiaBan());
                    setLau.setMoTa(request.getMoTa());
                    setLau.setMoTaChiTiet(request.getMoTaChiTiet());
                    setLau.setTrangThai(request.getTrangThai());
                    setLau.setIdLoaiSet(loaiLauRepository.findById(request.getIdLoaiSet()).get());
                    setLau.setHinhAnh(request.getHinhAnh());
                    setLauRepository.save(setLau);
                    return modelMapper.map(setLau, SetLauResponse.class);
                }).orElse(null);
    }
}
