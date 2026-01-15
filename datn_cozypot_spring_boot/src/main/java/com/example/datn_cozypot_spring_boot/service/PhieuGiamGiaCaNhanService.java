package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaCaNhanDTO;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhieuGiamGiaCaNhanService {
    @Autowired
    private PhieuGiamGiaCaNhanRepo repo;

    @Autowired
    private KhachHangRepo khachHangRepo;

    @Autowired
    private PhieuGiamGiaRepo phieuGiamGiaRepo;

    @Autowired
    private HoaDonThanhToanRepo hoaDonRepo;

    public List<PhieuGiamGiaCaNhanDTO> getAll() {
        List<PhieuGiamGiaCaNhanDTO> list = new ArrayList<>();

        for (PhieuGiamGiaCaNhan e : repo.findAll()) {
            list.add(convertToDTO(e));
        }

        return list;
    }

    public List<PhieuGiamGiaCaNhanDTO> search(String tenKH, Integer status) {
        List<PhieuGiamGiaCaNhanDTO> list = new ArrayList<>();

        for (PhieuGiamGiaCaNhan e : repo.search(tenKH, status)) {
            list.add(convertToDTO(e));
        }

        return list;
    }

    public PhieuGiamGiaCaNhan create(PhieuGiamGiaCaNhanDTO dto) {
        PhieuGiamGiaCaNhan entity = new PhieuGiamGiaCaNhan();

        mapData(dto, entity);

        entity.setNgayNhan(Instant.now());
        entity.setTrangThaiSuDung(0); // mới nhận

        return repo.save(entity);
    }

    public PhieuGiamGiaCaNhan update(Integer id, PhieuGiamGiaCaNhanDTO dto) {
        PhieuGiamGiaCaNhan entity = repo.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        mapData(dto, entity);

        return repo.save(entity);
    }

    private void mapData(PhieuGiamGiaCaNhanDTO dto, PhieuGiamGiaCaNhan entity) {

        KhachHang kh = khachHangRepo.findById(dto.getIdKhachHang())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        PhieuGiamGia pg = phieuGiamGiaRepo.findById(dto.getIdPhieuGiamGia())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        entity.setIdKhachHang(kh);
        entity.setIdPhieuGiamGia(pg);

        if (dto.getIdHoaDonThanhToan() != null) {
            HoaDonThanhToan hd = hoaDonRepo.findById(dto.getIdHoaDonThanhToan())
                    .orElse(null);
            entity.setIdHoaDonThanhToan(hd);
        }

        entity.setTrangThaiSuDung(dto.getTrangThaiSuDung());
        entity.setNgaySuDung(dto.getNgaySuDung());
        entity.setNgayHetHan(dto.getNgayHetHan());

        entity.setNguonGoc(dto.getNguonGoc());
        entity.setGhiChu(dto.getGhiChu());
    }

    private PhieuGiamGiaCaNhanDTO convertToDTO(PhieuGiamGiaCaNhan e) {
        PhieuGiamGiaCaNhanDTO dto = new PhieuGiamGiaCaNhanDTO();

        dto.setId(e.getId());
        dto.setMaGiamGiaCaNhan(e.getMaGiamGiaCaNhan());

        if (e.getIdKhachHang() != null) {
            dto.setIdKhachHang(e.getIdKhachHang().getId());
            dto.setTenKhachHang(e.getIdKhachHang().getTenKhachHang());
        }

        if (e.getIdPhieuGiamGia() != null) {
            dto.setIdPhieuGiamGia(e.getIdPhieuGiamGia().getId());
            dto.setTenPhieuGiamGia(e.getIdPhieuGiamGia().getTenPhieuGiamGia());
        }

        if (e.getIdHoaDonThanhToan() != null) {
            dto.setIdHoaDonThanhToan(e.getIdHoaDonThanhToan().getId());
        }

        dto.setNgayNhan(e.getNgayNhan());
        dto.setNgaySuDung(e.getNgaySuDung());
        dto.setTrangThaiSuDung(e.getTrangThaiSuDung());
        dto.setNgayHetHan(e.getNgayHetHan());
        dto.setNguonGoc(e.getNguonGoc());
        dto.setGhiChu(e.getGhiChu());

        return dto;
    }
}
