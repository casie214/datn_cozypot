package com.example.datn_cozypot_spring_boot.service.implementation;

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
import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.setLauChiTiet.SetLauChiTietResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.*;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonAnServiceImplementation implements MonAnService {
    private final MonAnRepository monAnRepository;
    private final MonAnChiTietRepository monAnChiTietRepository;
    private final SetLauRepository setLauRepository;
    private final LoaiLauRepository loaiLauRepository;
    private final SetLauChiTietRepository setLauChiTietRepository;
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
        return danhMucChiTietRepository.findAll().stream()
                .map(entity -> {
                    DanhMucChiTietResponse response = modelMapper.map(entity, DanhMucChiTietResponse.class);
                    response.setTenDanhMucChiTiet(entity.getTenDanhMucChiTiet());
                    if (entity.getIdDanhMuc() != null) {
                        response.setTenDanhMuc(entity.getIdDanhMuc().getTenDanhMuc());
                    } else {
                        response.setTenDanhMuc("---");
                    }
                    return response;
                })
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
    @Transactional
    public SetLauResponse createSetLau(SetLauRequest request) {
        SetLau setLau = new SetLau();
        setLau.setTenSetLau(request.getTenSetLau());
        setLau.setGiaBan(request.getGiaBan());
        setLau.setHinhAnh(request.getHinhAnh());
        setLau.setMoTa(request.getMoTa());
        setLau.setTrangThai(request.getTrangThai());
        setLau.setNgayTao(Instant.now());

        if (request.getIdLoaiSet() != null) {
            LoaiSetLau loaiSet = loaiLauRepository.findById(request.getIdLoaiSet())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại set!"));
            setLau.setIdLoaiSet(loaiSet);
        }

        SetLau savedSetLau = setLauRepository.save(setLau);

        if (request.getListChiTietSetLau() != null && !request.getListChiTietSetLau().isEmpty()) {

            List<ChiTietSetLau> listToSave = new ArrayList<>();

            for (SetLauChiTietRequest itemRequest : request.getListChiTietSetLau()) {
                ChiTietMonAn monAn = monAnChiTietRepository.findById(itemRequest.getIdChiTietMonAn())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID: " + itemRequest.getIdChiTietMonAn()));

                ChiTietSetLau detail = new ChiTietSetLau();
                detail.setSetLau(savedSetLau);
                detail.setChiTietMonAn(monAn);
                detail.setSoLuong(itemRequest.getSoLuong());

                listToSave.add(detail);
            }
            setLauChiTietRepository.saveAll(listToSave);
        }

        return new SetLauResponse();
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
    @Transactional
    public SetLauResponse putLau(int id, SetLauRequest request) {
        SetLau existingSet = setLauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID: " + id));

        existingSet.setTenSetLau(request.getTenSetLau());
        existingSet.setGiaBan(request.getGiaBan());
        existingSet.setHinhAnh(request.getHinhAnh());
        existingSet.setMoTa(request.getMoTa());
        existingSet.setTrangThai(request.getTrangThai());

        if (request.getIdLoaiSet() != null) {
            LoaiSetLau loaiSet = loaiLauRepository.findById(request.getIdLoaiSet())
                    .orElseThrow(() -> new RuntimeException("Loại set không tồn tại"));
            existingSet.setIdLoaiSet(loaiSet);
        }

        existingSet.getListChiTietSetLau().clear();

        if (request.getListChiTietSetLau() != null) {
            for (SetLauChiTietRequest itemReq : request.getListChiTietSetLau()) {
                ChiTietMonAn monAn = monAnChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                        .orElseThrow(() -> new RuntimeException("Món ăn ID " + itemReq.getIdChiTietMonAn() + " không tồn tại"));

                ChiTietSetLau newDetail = new ChiTietSetLau();
                newDetail.setSetLau(existingSet);
                newDetail.setChiTietMonAn(monAn);
                newDetail.setSoLuong(itemReq.getSoLuong());

                existingSet.getListChiTietSetLau().add(newDetail);
            }
        }
        SetLau savedSet = setLauRepository.save(existingSet);
        return modelMapper.map(savedSet, SetLauResponse.class);
    }

    @Override
    public DanhMucResponse putDanhMuc(int id, DanhMucRequest request) {
        return danhMucRepository.findById(id)
                .map(danhMuc -> {
                    danhMuc.setTenDanhMuc(request.getTenDanhMuc());
                    danhMuc.setMoTa(request.getMoTa());
                    danhMuc.setTrangThai(request.getTrangThai());
                    danhMucRepository.save(danhMuc);
                    return modelMapper.map(danhMuc, DanhMucResponse.class);
                })
                .orElse(null);
    }

    @Override
    public DanhMucResponse addNewDanhMuc(DanhMucRequest request) {
        DanhMuc danhMuc = modelMapper.map(request, DanhMuc.class);
        danhMuc.setId(null);
        danhMuc.setMaDanhMuc(null);
        danhMucRepository.save(danhMuc);
        return modelMapper.map(danhMuc, DanhMucResponse.class);
    }

    @Override
    public LoaiLauResponse putLoaiLau(int id, LoaiLauRequest request) {
        return loaiLauRepository.findById(id)
                .map(loaiSetLau -> {
                    loaiSetLau.setTenLoaiSet(request.getTenLoaiSet());
                    loaiSetLau.setMoTa(request.getMoTa());
                    loaiSetLau.setTrangThai(request.getTrangThai());
                    loaiLauRepository.save(loaiSetLau);
                    return modelMapper.map(loaiSetLau, LoaiLauResponse.class);
                })
                .orElse(null);
    }

    @Override
    public LoaiLauResponse addNewLoaiLau(LoaiLauRequest request) {
        LoaiSetLau loaiSetLau = modelMapper.map(request, LoaiSetLau.class);
        loaiSetLau.setId(null);
        loaiSetLau.setMaLoaiSet(null);
        loaiLauRepository.save(loaiSetLau);
        return modelMapper.map(loaiSetLau, LoaiLauResponse.class);
    }

    @Override
    public DanhMucChiTietResponse putDanhMucChiTiet(int id, DanhMucChiTietRequest request) {
        return danhMucChiTietRepository.findById(id)
                .map(danhMucChiTiet -> {
                    danhMucChiTiet.setIdDanhMuc(danhMucRepository.findById(request.getIdDanhMuc()).get());
                    danhMucChiTiet.setTenDanhMucChiTiet(request.getTenDanhMucChiTiet());
                    danhMucChiTiet.setMoTa(request.getMoTa());
                    danhMucChiTiet.setTrangThai(request.getTrangThai());
                    danhMucChiTietRepository.save(danhMucChiTiet);
                    return modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class);
                })
                .orElse(null);
    }

    @Override
    public DanhMucChiTietResponse addNewDanhMucChiTiet(DanhMucChiTietRequest request) {
        DanhMucChiTiet danhMucChiTiet = modelMapper.map(request, DanhMucChiTiet.class);
        danhMucChiTiet.setId(null);
        danhMucChiTiet.setMaDanhMucChiTiet(null);
        danhMucChiTietRepository.save(danhMucChiTiet);
        return modelMapper.map(danhMucChiTiet, DanhMucChiTietResponse.class);
    }

    @Override
    @Transactional // Quan trọng để tránh lỗi Lazy Loading
    public SetLauResponse findSetLauById(int id) {
        // 1. Tìm Entity SetLau
        SetLau setLau = setLauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID: " + id));

        // 2. Map các trường cơ bản sang DTO
        SetLauResponse response = new SetLauResponse();
        response.setId(setLau.getId());
        response.setTenSetLau(setLau.getTenSetLau());
        response.setGiaBan(setLau.getGiaBan());
        response.setHinhAnh(setLau.getHinhAnh());
        response.setMoTa(setLau.getMoTa());
        response.setTrangThai(setLau.getTrangThai());

        // Map Loại Set
        if (setLau.getIdLoaiSet() != null) {
            LoaiLauResponse loaiRes = new LoaiLauResponse();
            loaiRes.setId(setLau.getIdLoaiSet().getId());
            loaiRes.setTenLoaiSet(setLau.getIdLoaiSet().getTenLoaiSet());
            response.setIdLoaiSet(loaiRes.getId());
        }

        List<SetLauChiTietResponse> listDetailResponse = new ArrayList<>();

        if (setLau.getListChiTietSetLau() != null) {
            for (ChiTietSetLau detailEntity : setLau.getListChiTietSetLau()) {
                SetLauChiTietResponse detailDTO = new SetLauChiTietResponse();
                detailDTO.setId(detailEntity.getId());
                detailDTO.setSoLuong(detailEntity.getSoLuong());

                ChiTietMonAn monAn = detailEntity.getChiTietMonAn();
                if (monAn != null) {
                    MonAnChiTietResponse monAnDTO = new MonAnChiTietResponse();
                    monAnDTO.setId(monAn.getId());
                    monAnDTO.setTenChiTietMonAn(monAn.getTenChiTietMonAn());
                    monAnDTO.setGiaBan(monAn.getGiaBan());
                    monAnDTO.setDonVi(monAn.getDonVi());
                    monAnDTO.setHinhAnh(monAn.getHinhAnh());

                    detailDTO.setChiTietMonAn(monAnDTO);
                }

                listDetailResponse.add(detailDTO);
            }
        }

        response.setListChiTietSetLau(listDetailResponse);

        return response;
    }
}
