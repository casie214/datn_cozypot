package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaDTO;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepo;
import com.example.datn_cozypot_spring_boot.repository.PhieuGiamGiaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhieuGiamGiaService {

        @Autowired
        private PhieuGiamGiaRepo phieuRepo;

        @Autowired
        private DotKhuyenMaiRepo dotRepo;

        public List<PhieuGiamGiaDTO> getAll() {
            List<PhieuGiamGiaDTO> dtos = new ArrayList<>();
            for (PhieuGiamGia p : phieuRepo.findAll()) {
                dtos.add(convertToDto(p));
            }
            return dtos;
        }

        public PhieuGiamGia create(PhieuGiamGiaDTO dto) {

            validateDate(dto);

            PhieuGiamGia entity = new PhieuGiamGia();
            mapData(dto, entity);

            entity.setNgayTao(Instant.now());
            entity.setSoLuongDaDung(0);
            entity.setNguoiTao("Admin");

            return phieuRepo.save(entity);
        }

        public PhieuGiamGia update(Integer id, PhieuGiamGiaDTO dto) {

            PhieuGiamGia entity = phieuRepo.findById(id).orElse(null);
            if (entity == null) {
                throw new RuntimeException("Không tìm thấy phiếu giảm giá ID: " + id);
            }

            validateDate(dto);

            mapData(dto, entity);

            entity.setNgaySua(Instant.now());
            entity.setNguoiSua("Admin");

            return phieuRepo.save(entity);
        }

        private void validateDate(PhieuGiamGiaDTO dto) {
            if (dto.getNgayKetThuc().isBefore(dto.getNgayBatDau())) {
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
            }
        }

        private void mapData(PhieuGiamGiaDTO dto, PhieuGiamGia entity) {

            entity.setTenPhieuGiamGia(dto.getTenPhieuGiamGia().trim());

            entity.setCodeGiamGia(dto.getCodeGiamGia());

            entity.setLoaiGiamGia(dto.getLoaiGiamGia());
            entity.setGiaTriGiam(dto.getGiaTriGiam());

            entity.setGiaTriGiamToiDa(
                    dto.getGiaTriGiamToiDa() != null
                            ? dto.getGiaTriGiamToiDa()
                            : BigDecimal.ZERO
            );

            entity.setDonHangToiThieu(
                    dto.getDonHangToiThieu() != null
                            ? dto.getDonHangToiThieu()
                            : BigDecimal.ZERO
            );

            entity.setNgayBatDau(dto.getNgayBatDau());
            entity.setNgayKetThuc(dto.getNgayKetThuc());

            entity.setSoLuongPhatHanh(dto.getSoLuongPhatHanh());

            entity.setTrangThai(dto.getTrangThai());

            if (dto.getIdDotKhuyenMai() != null) {
                DotKhuyenMai dk = dotRepo.findById(dto.getIdDotKhuyenMai()).orElse(null);
                entity.setIdDotKhuyenMai(dk);
            }
        }

        public List<PhieuGiamGiaDTO> searchAdvanced(String keyword, Integer status) {

            String searchKey =
                    (keyword == null || keyword.trim().isEmpty())
                            ? null
                            : keyword.trim();

            List<PhieuGiamGiaDTO> dtos = new ArrayList<>();

            for (PhieuGiamGia p : phieuRepo.searchFilter(searchKey, status)) {
                dtos.add(convertToDto(p));
            }

            return dtos;
        }

        private PhieuGiamGiaDTO convertToDto(PhieuGiamGia p) {

            PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();

            dto.setId(p.getId());

            dto.setMaPhieuGiamGia(p.getMaPhieuGiamGia());
            dto.setCodeGiamGia(p.getCodeGiamGia());

            dto.setTenPhieuGiamGia(p.getTenPhieuGiamGia());
            dto.setLoaiGiamGia(p.getLoaiGiamGia());
            dto.setGiaTriGiam(p.getGiaTriGiam());
            dto.setGiaTriGiamToiDa(p.getGiaTriGiamToiDa());
            dto.setDonHangToiThieu(p.getDonHangToiThieu());

            dto.setNgayBatDau(p.getNgayBatDau());
            dto.setNgayKetThuc(p.getNgayKetThuc());

            dto.setSoLuongPhatHanh(p.getSoLuongPhatHanh());
            dto.setSoLuongDaDung(p.getSoLuongDaDung());

            dto.setTrangThai(p.getTrangThai());

            dto.setNgayTao(p.getNgayTao());
            dto.setNgaySua(p.getNgaySua());

            dto.setNguoiTao(p.getNguoiTao());
            dto.setNguoiSua(p.getNguoiSua());

            if (p.getIdDotKhuyenMai() != null) {
                dto.setIdDotKhuyenMai(p.getIdDotKhuyenMai().getId());
            }

            return dto;
        }
}
